package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.info.BundleDependency;
import de.luckymcdev.foundryengine.common.bundle.modcompat.BundleModContainer;
import de.luckymcdev.foundryengine.common.bundle.modcompat.BundleModFileInfo;
import de.luckymcdev.foundryengine.common.bundle.modcompat.BundleModInfo;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.common.script.GroovyScriptLoader;
import de.luckymcdev.foundryengine.common.util.ErrorHandler;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bundle Manager that manages Bundle Lifecycles.
 *
 * <p>Script loading is sided:
 * <ul>
 *   <li>{@link #register(Bundle)} always loads <em>common</em> scripts.</li>
 *   <li>{@link #loadClientScripts()} must be called from the client dist entrypoint.</li>
 *   <li>{@link #loadServerScripts()} must be called from the server dist entrypoint.</li>
 * </ul>
 */
public class BundleManager implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ReentrantLock reloadLock = new ReentrantLock();
	private final GenericRegistry<String, Bundle> bundles = new GenericRegistry<>();
	private final List<ModContainer> bundleContainers = new ArrayList<>();
	private final Set<String> registeredConfigs = ConcurrentHashMap.newKeySet();
	private final Set<String> failedBundleIds = ConcurrentHashMap.newKeySet();
	private final BundleDiscovery bundleDiscovery;
	private final GroovyScriptLoader scriptLoader;
	private final BundleLifecycleDispatcher lifecycleDispatcher = new BundleLifecycleDispatcher();
	private volatile boolean reloading = false;
	private @Nullable MinecraftServer server;

	public BundleManager(IEventBus modBus) {
		BundleFactory bundleFactory = new BundleFactory(modBus);
		this.scriptLoader = bundleFactory.getScriptLoader();
		this.bundleDiscovery = new BundleDiscovery(bundleFactory, this::register);
	}

	private static BundleModContainer createModContainer(Bundle bundle) {
		var bundleInfo = bundle.info();
		var modInfo = new BundleModInfo(bundleInfo, null);
		var owningFile = new BundleModFileInfo(modInfo, bundleInfo, modInfo);
		modInfo.setOwningFile(owningFile);
		return new BundleModContainer(modInfo, bundle);
	}

	public void setServer(@Nullable MinecraftServer server) {
		this.server = server;
	}

	/**
	 * Registers a bundle and immediately loads its common-side scripts.
	 */
	public void register(Bundle bundle) {
		bundles.register(bundle.info().id(), bundle);
		var container = createModContainer(bundle);
		bundleContainers.add(container);

		String bundleId = bundle.info().id();
		boolean depsFailed = false;
		for (BundleDependency dep : bundle.info().dependencies()) {
			if (dep.type() == BundleDependency.Type.BUNDLE && failedBundleIds.contains(dep.id())) {
				LOGGER.error("Skipping script loading for bundle '{}' because its dependency '{}' failed to load scripts.", bundleId, dep.id());
				failedBundleIds.add(bundleId);
				depsFailed = true;

				String msg = String.format("Bundle '%s' skipped script loading because its dependency '%s' failed to load scripts.", bundleId, dep.id());
				ModLoader.addLoadingIssue(ModLoadingIssue.warning(msg));
				break;
			}
		}

		if (!depsFailed) {
			try {
				bundle.loadCommon(scriptLoader);
			} catch (Exception e) {
				BundleExceptionHandler.handle(
					"Failed to load common scripts for bundle '" + bundleId + "'", e);
			}
		}

		var spec = bundle.configSpec().build();
		if (!registeredConfigs.contains(bundleId)) {
			container.registerConfig(ModConfig.Type.COMMON, spec);
			registeredConfigs.add(bundleId);
		} else {
			// Config is already registered in ConfigTracker from a previous load;
			// re-registering would throw a "config file conflict" on reload.
			container.setConfigSpec(spec);
		}
		lifecycleDispatcher.fireLoaded(bundle);
		LOGGER.debug("Registered Bundle: {} with Info: {}", bundle.info().id(), bundle.info());
	}

	public List<ModContainer> getBundleContainers() {
		return List.copyOf(bundleContainers);
	}

	/**
	 * Rebuilds NeoForge's global loaded-mod list so it reflects the current set of
	 * bundle containers. The construct-mod event only runs once, so without this the
	 * list would keep stale {@link BundleModContainer}s after a reload.
	 */
	public void refreshModList() {
		ModList modList = ModList.get();
		if (modList == null || bundleContainers.isEmpty()) {
			return;
		}
		List<ModContainer> allContainers = new ArrayList<>();
		for (var container : modList.getSortedMods()) {
			if (!(container instanceof BundleModContainer)) {
				allContainers.add(container);
			}
		}
		allContainers.addAll(bundleContainers);
		try {
			var method = ModList.class.getDeclaredMethod("setLoadedMods", List.class);
			method.setAccessible(true);
			method.invoke(modList, allContainers);
		} catch (Exception e) {
			// Fail-safe: never let mod-list reflection crash a reload. The loaded mod
			// list may be stale until restart, but the reload itself must survive.
			Throwable cause = e instanceof InvocationTargetException ite ? ite.getTargetException() : e;
			LOGGER.error("Failed to refresh loaded mod list via reflection; the loaded mod list may be stale. Cause: {}",
				cause, e);
		}
	}

	/**
	 * Loads client-side scripts for all registered bundles.
	 * Call this from {@code FoundryEngineModClient} during client setup.
	 */
	public void loadClientScripts() {
		for (Bundle bundle : bundles.values()) {
			String bundleId = bundle.info().id();
			if (failedBundleIds.contains(bundleId)) {
				continue;
			}
			try {
				bundle.loadClient(scriptLoader);
			} catch (Exception e) {
				BundleExceptionHandler.handle(
					"Failed to load client scripts for bundle '" + bundleId + "'", e);
			}
		}
	}

	/**
	 * Loads server-side scripts for all registered bundles.
	 * Call this from {@code FoundryEngineModServer} during server setup.
	 */
	public void loadServerScripts() {
		for (Bundle bundle : bundles.values()) {
			String bundleId = bundle.info().id();
			if (failedBundleIds.contains(bundleId)) {
				continue;
			}
			try {
				bundle.loadServer(scriptLoader);
			} catch (Exception e) {
				BundleExceptionHandler.handle(
					"Failed to load server scripts for bundle '" + bundleId + "'", e);
			}
		}
	}

	public void reportScriptErrors() {
		Map<String, List<Throwable>> errors = GroovyScriptLoader.getAndClearErrors();
		for (Map.Entry<String, List<Throwable>> entry : errors.entrySet()) {
			String bundleId = entry.getKey();
			failedBundleIds.add(bundleId);
			for (Throwable t : entry.getValue()) {
				if (t instanceof MultipleCompilationErrorsException mce) {
					for (var errorObj : mce.getErrorCollector().getErrors()) {
						String detail = errorObj.toString();
						if (errorObj instanceof SyntaxErrorMessage sem) {
							detail = String.format("%s (line %d, col %d)",
								sem.getCause().getMessage(), sem.getCause().getLine(), sem.getCause().getStartColumn());
						}
						String message = String.format("Bundle '%s' script compilation failed: %s", bundleId, detail);
						ModLoader.addLoadingIssue(ModLoadingIssue.warning(message).withCause(t));
					}
				} else {
					String humanMsg = ErrorHandler.getFormattedMessage(t);
					String type = t.getClass().getSimpleName();
					StackTraceElement scriptFrame = ErrorHandler.findScriptFrame(t);
					String loc = scriptFrame != null ? " at " + scriptFrame.getFileName() + ":" + scriptFrame.getLineNumber() : "";

					String message = String.format("Bundle '%s' script error: %s [%s]%s", bundleId, humanMsg, type, loc);
					ModLoader.addLoadingIssue(ModLoadingIssue.warning(message).withCause(t));
				}
			}
		}
	}

	/**
	 * Removes and cleans up a bundle, including closing its ZIP FileSystem if applicable.
	 */
	public void remove(Bundle bundle) {
		bundles.remove(bundle.info().id());
		unloadBundle(bundle);
	}

	/**
	 * Discovers and loads all bundles from the specified directory.
	 */
	public void discover(Path directory) throws IOException {
		bundleDiscovery.discover(directory);
	}

	/**
	 * Returns all currently loaded bundles.
	 */
	public Collection<Bundle> getBundles() {
		return bundles.values();
	}

	/**
	 * Retrieves a bundle by its ID.
	 */
	public Bundle getBundle(String id) {
		return bundles.get(id);
	}

	/**
	 * Returns true if any bundles have been loaded.
	 */
	public boolean anyBundles() {
		return !bundles.isEmpty();
	}

	/**
	 * Reloads all bundles by clearing all script event callbacks, unloading current
	 * bundles, rediscovering them from disk, and re-loading common + server scripts.
	 */
	public void reload() {
		if (reloading) {
			return;
		}
		reloadLock.lock();
		try {
			reloading = true;
			LOGGER.info("Reloading FoundryEngine Bundles...");
			lifecycleDispatcher.fireReloadStarted();

			Common.clearEvents();

			unloadAllBundles();
			bundles.clear();
			// Drop the config bookkeeping from the previous load so re-registration
			// below takes the fresh path and stays consistent with ConfigTracker.
			registeredConfigs.clear();

			Common.getScriptShell().invalidateAll();
			scriptLoader.invalidateCache();
			failedBundleIds.clear();

			try {
				discover(Common.BUNDLES);
			} catch (IOException e) {
				BundleExceptionHandler.handle("Failed to reload bundles", e);
			}

			refreshModList();
			loadServerScripts();
			reportScriptErrors();

			if (server != null) {
				// Commands are normally registered once at server start; re-posting the
				// event here cannot re-run all of NeoForge's registry logic, so flag that
				// command/registry state may be inconsistent until the server restarts.
				LOGGER.warn("Reloading FoundryEngine Bundles while a server is live: command/registry state "
					+ "may be inconsistent until restart.");
				var commands = server.getCommands();
				if (commands != null && commands.getDispatcher() != null) {
					var selection = Commands.CommandSelection.ALL;
					var buildContext = CommandBuildContext.simple(
						server.registryAccess(), server.getWorldData().enabledFeatures());
					NeoForge.EVENT_BUS.post(new RegisterCommandsEvent(
						commands.getDispatcher(), selection, buildContext));
				}
			}

			lifecycleDispatcher.fireReloadCompleted();
		} finally {
			reloading = false;
			reloadLock.unlock();
		}
	}

	private void unloadAllBundles() {
		for (Bundle bundle : bundles.values()) {
			unloadBundle(bundle);
		}
	}

	/**
	 * Unloads a single bundle: calls onUnload on all entrypoints,
	 * removes its config from ConfigTracker, cleans up the mod container,
	 * and closes the ZIP FileSystem if present.
	 */
	private void unloadBundle(Bundle bundle) {
		lifecycleDispatcher.firePreUnload(bundle);
		String id = bundle.info().id();
		bundleContainers.removeIf(c ->
			c instanceof BundleModContainer bmc && bmc.getBundle().info().id().equals(id));
		// Remove the stale ModConfig so a reload can re-register the config cleanly.
		removeBundleConfigFromTracker(id);
		registeredConfigs.remove(id);
		try {
			bundle.unload();
		} finally {
			lifecycleDispatcher.fireUnloaded(bundle);
			closeFileSystem(bundle);
		}
	}

	/**
	 * Removes a bundle's {@link ModConfig} from {@link ConfigTracker} so that a reload
	 * can re-register it with a freshly built spec. {@link ConfigTracker} has no public
	 * removal API, so this reflects into its bookkeeping maps. Fails safe: on any
	 * reflection error the stale config is left in place and the reload continues.
	 */
	private void removeBundleConfigFromTracker(String bundleId) {
		if (bundleId == null || bundleId.isEmpty()) {
			return;
		}
		String fileName = String.format(Locale.ROOT, "%s-%s.toml", bundleId, ModConfig.Type.COMMON.extension());
		try {
			ConfigTracker tracker = ConfigTracker.INSTANCE;

			Field fileMapField = ConfigTracker.class.getDeclaredField("fileMap");
			fileMapField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, ModConfig> fileMap = (Map<String, ModConfig>) fileMapField.get(tracker);
			ModConfig removed = fileMap.remove(fileName);
			if (removed == null) {
				return;
			}

			Field configSetsField = ConfigTracker.class.getDeclaredField("configSets");
			configSetsField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<ModConfig.Type, Set<ModConfig>> configSets =
				(Map<ModConfig.Type, Set<ModConfig>>) configSetsField.get(tracker);
			Set<ModConfig> byType = configSets.get(removed.getType());
			if (byType != null) {
				byType.remove(removed);
			}

			Field configsByModField = ConfigTracker.class.getDeclaredField("configsByMod");
			configsByModField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, List<ModConfig>> configsByMod =
				(Map<String, List<ModConfig>>) configsByModField.get(tracker);
			List<ModConfig> byMod = configsByMod.get(bundleId);
			if (byMod != null) {
				byMod.remove(removed);
			}

			LOGGER.debug("Removed stale bundle config '{}' from ConfigTracker", fileName);
		} catch (ReflectiveOperationException | RuntimeException e) {
			LOGGER.error("Failed to remove stale bundle config '{}' from ConfigTracker", fileName, e);
		}
	}

	private void closeFileSystem(Bundle bundle) {
		FileSystem fs = bundle.bundleFiles().zipFileSystem();
		if (bundle.bundleFiles().hasZipFileSystem()) {
			try {
				fs.close();
			} catch (IOException e) {
				LOGGER.warn("Failed to close ZIP FileSystem for bundle '{}': {}",
					bundle.info().id(), e.getLocalizedMessage());
			}
		}
	}

	public BundleLifecycleDispatcher getLifecycleDispatcher() {
		return lifecycleDispatcher;
	}

	public BundleDiscovery getBundleDiscovery() {
		return bundleDiscovery;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.reload();
	}
}