package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.event.*;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.common.script.BundleScriptLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collection;

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
    private final Object reloadLock = new Object();
    private final GenericRegistry<String, Bundle> bundles = new GenericRegistry<>();
    private final BundleDiscovery bundleDiscovery;
    private final BundleScriptLoader scriptLoader;
    private final BundleLifecycleDispatcher lifecycleDispatcher = new BundleLifecycleDispatcher();
    private MinecraftServer server;

    public BundleManager(IEventBus modBus, Path configDirectory) {
        BundleFactory bundleFactory = new BundleFactory(modBus, configDirectory);
        this.scriptLoader = bundleFactory.getScriptLoader();
        this.bundleDiscovery = new BundleDiscovery(bundleFactory, this::register);
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Registers a bundle and immediately loads its common-side scripts.
     */
    public void register(Bundle bundle) {
        bundles.register(bundle.info().id(), bundle);
        try {
            bundle.loadCommon(scriptLoader);
        } catch (Exception e) {
            BundleExceptionHandler.handle(
                    "Failed to load common scripts for bundle '" + bundle.info().id() + "'", e);
        }
        lifecycleDispatcher.fireLoaded(bundle);
        LOGGER.debug("Registered Bundle: {} with Info: {}", bundle.info().id(), bundle.info());
    }

    /**
     * Loads client-side scripts for all registered bundles.
     * Call this from {@code FoundryEngineModClient} during client setup.
     */
    public void loadClientScripts() {
        for (Bundle bundle : bundles.values()) {
            try {
                bundle.loadClient(scriptLoader);
            } catch (Exception e) {
                BundleExceptionHandler.handle(
                        "Failed to load client scripts for bundle '" + bundle.info().id() + "'", e);
            }
        }
    }

    /**
     * Loads server-side scripts for all registered bundles.
     * Call this from {@code FoundryEngineModServer} during server setup.
     */
    public void loadServerScripts() {
        for (Bundle bundle : bundles.values()) {
            try {
                bundle.loadServer(scriptLoader);
            } catch (Exception e) {
                BundleExceptionHandler.handle(
                        "Failed to load server scripts for bundle '" + bundle.info().id() + "'", e);
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
     * Reloads all bundles by clearing all script event callbacks, unloading current
     * bundles, rediscovering them from disk, and re-loading common + server scripts.
     */
    public void reload() {
        synchronized (reloadLock) {
            LOGGER.info("Reloading FoundryEngine Bundles...");
            lifecycleDispatcher.fireReloadStarted();

            AreaEvents.Internal.clear();
            BlockEvents.Internal.clear();
            BundleEvents.Internal.clear();
            ClientEvents.Internal.clear();
            CommandEvents.Internal.clear();
            EntityEvents.Internal.clear();
            ItemEvents.Internal.clear();
            LevelEvents.Internal.clear();
            NetworkEvents.Internal.clear();
            PlayerEvents.Internal.clear();
            RecipeEvents.Internal.clear();

            unloadAllBundles();
            bundles.clear();

            try {
                discover(Common.BUNDLES);
            } catch (IOException e) {
                BundleExceptionHandler.handle("Failed to reload bundles", e);
            }

            loadServerScripts();

            if (server != null) {
                var dispatcher = server.getCommands().getDispatcher();
                var selection = Commands.CommandSelection.ALL;
                var buildContext = CommandBuildContext.simple(server.registryAccess(), server.getWorldData().enabledFeatures());
                NeoForge.EVENT_BUS.post(new RegisterCommandsEvent(dispatcher, selection, buildContext));
            }

            lifecycleDispatcher.fireReloadCompleted();
        }
    }

    private void unloadAllBundles() {
        for (Bundle bundle : bundles.values()) {
            unloadBundle(bundle);
        }
    }

    /**
     * Unloads a single bundle: saves config, calls onUnload on all entrypoints,
     * and closes the ZIP FileSystem if present.
     */
    private void unloadBundle(Bundle bundle) {
        try {
            lifecycleDispatcher.firePreUnload(bundle);

            if (bundle.bundleConfig().isLoaded()) {
                bundle.bundleConfig().save();
            }

            bundle.unload();
        } finally {
            closeFileSystem(bundle);
            lifecycleDispatcher.fireUnloaded(bundle);
        }
    }

    private void closeFileSystem(Bundle bundle) {
        FileSystem fs = bundle.bundleFiles().zipFileSystem();
        if (fs != null && fs.isOpen()) {
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