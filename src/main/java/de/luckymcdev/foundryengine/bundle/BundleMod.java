package de.luckymcdev.foundryengine.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.bundle.common.modcompat.BundleModContainer;
import de.luckymcdev.foundryengine.config.StartupConfig;
import de.luckymcdev.foundryengine.data.common.gen.BundleDataGenerator;
import de.luckymcdev.foundryengine.debug.client.screen.BundleDebugEntry;
import de.luckymcdev.foundryengine.event.common.BundleEvents;
import de.luckymcdev.foundryengine.network.common.packet.BundleHashPacket;
import de.luckymcdev.foundryengine.pack.server.DynamicPackRepository;
import de.luckymcdev.foundryengine.pack.server.ResourcePackMerger;
import de.luckymcdev.foundryengine.script.client.MaliciousScriptScreen;
import de.luckymcdev.foundryengine.script.common.sandbox.SandboxWaiver;
import de.luckymcdev.foundryengine.util.common.EngineModPart;
import de.luckymcdev.foundryengine.util.common.FolderHash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.*;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Owns bundle discovery, resource-pack injection and script lifecycle. */
@Mod(BundleMod.MODID)
public final class BundleMod implements EngineModPart {
	public static final String MODID = "foundryengine_bundle";
	private static final Logger LOGGER = LogUtils.getLogger();
	private static boolean handledClassFilter = false;

	public BundleMod(IEventBus modBus, ModContainer modContainer) {
		BundleEvents.Internal.register(bus());
		this.wire(modBus);
		var b = bus();

		modBus.addListener(this::onPostInit);
		modBus.addListener(this::onAddPackFinders);

		b.addListener(this::onServerAboutToStart);
		b.addListener(this::onAddReloadListeners);
		b.addListener(this::onPlayerLoggedIn);
		b.addListener(this::onServerStarting);
		b.addListener(this::onServerStopping);

		if (FMLEnvironment.getDist().isClient()) {
			modBus.addListener(this::onRegisterDebugEntry);
			b.addListener(this::onScreenOpening);
			b.addListener(this::onLoggingIn);
		}
	}

	@Override
	public void onConstruct(FMLConstructModEvent event) {
		try {
			Common.getBundleManager().discover(Common.BUNDLES);
			Common.getBundleManager().refreshModList();
			Common.getBundleManager().reportScriptErrors();
		} catch (IOException e) {
			LOGGER.error("Error while loading bundles", e);
		}
		extractGradleTemplates();
	}

	@Override
	public void onDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
		BundleEvents.Internal.postDedicatedServerSetup(event);
	}

	@Override
	public void onCommonSetup(FMLCommonSetupEvent event) {
		Common.getNetworkManager().register(BundleHashPacket.DEFINITION);
		BundleEvents.Internal.postCommonSetup(event);
		BundleDataGenerator.runAll();
	}

	@Override
	public void onClientSetup(FMLClientSetupEvent event) {
		BundleEvents.Internal.postClientSetup(event);
		event.enqueueWork(() -> {
			Common.getBundleManager().loadClientScripts();
			Common.getBundleManager().reportScriptErrors();
			for (var container : Common.getBundleManager().getBundleContainers()) {
				if (container instanceof BundleModContainer) {
					container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
				}
			}
		});
	}

	private void extractGradleTemplates() {
		var files = List.of("build.gradle", "settings.gradle", "gradle.properties", ".gitignore");
		for (String fileName : files) {
			String path = "assets/foundryengine/gradle/" + fileName;
			try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
				if (stream != null) {
					Files.copy(stream, Common.GAMEDIR.resolve(fileName));
					LOGGER.debug("Extracted gradle template: {}", fileName);
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to extract gradle template {}: {}", fileName, e.getMessage());
			}
		}
	}

	private void onPostInit(InterModProcessEvent event) {
		BundleEvents.Internal.postPostInit(event);
	}

	private void onAddPackFinders(AddPackFindersEvent event) {
		PackType type = event.getPackType();
		var bundles = new DynamicPackRepository(type, "foundryengine/bundles", "FoundryEngine: Bundles",
			"Foundry Engine Bundle Resource Files",
			() -> Common.getBundleManager().getBundles().stream()
				.map(b -> type == PackType.CLIENT_RESOURCES ? b.bundleFiles().assets() : b.bundleFiles().data())
				.filter(Files::exists).toList(), Pack.Position.TOP, false);
		Path genPath = type == PackType.CLIENT_RESOURCES
			? BundleDataGenerator.getGeneratedAssetsPath() : BundleDataGenerator.getGeneratedDataPath();
		try { Files.createDirectories(genPath); }
		catch (IOException e) { LOGGER.error("Could not create generated dir {}", genPath, e); }
		var generated = new DynamicPackRepository(type, "foundryengine/bundles_generated", "FoundryEngine: Generated",
			"Foundry Engine Generated Resource Files", () -> List.of(genPath), Pack.Position.BOTTOM, true);
		event.addRepositorySource(bundles);
		event.addRepositorySource(generated);
	}

	private void onServerAboutToStart(ServerAboutToStartEvent event) {
		Common.getBundleManager().setServer(event.getServer());
	}

	private void onAddReloadListeners(AddServerReloadListenersEvent event) {
		event.addListener(Common.id("bundle_manager"), Common.getBundleManager());
	}

	private void onServerStarting(ServerStartingEvent event) {
		Common.getBundleManager().loadServerScripts();
		Common.getBundleManager().reportScriptErrors();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getBundleManager().setServer(null);
	}

	private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		LOGGER.debug("Player logged in, should send resource pack");
		var tempPath = Common.TEMP_DIR.resolve("resourcepack-out");
		ResourcePackMerger.mergeToZip(tempPath.resolve("fe-resources.zip"));
	}

	private void onRegisterDebugEntry(RegisterDebugEntriesEvent event) {
		event.register(Common.id("bundles_info"), new BundleDebugEntry(Common.getBundleManager()));
	}

	private void onScreenOpening(ScreenEvent.Opening event) {
		if (handledClassFilter || !(event.getNewScreen() instanceof TitleScreen)) return;
		handledClassFilter = true;
		checkMaliciousScriptConfig();
	}

	private void checkMaliciousScriptConfig() {
		if (StartupConfig.DISABLE_CLASS_FILTER.get() && !SandboxWaiver.isAccepted()) {
			var parent = Client.getCurrentScreen();
			Minecraft.getInstance().execute(() ->
				Client.setScreen(new MaliciousScriptScreen(parent, this::onClassFilterAccepted, this::onClassFilterDeclined)));
		}
	}

	private void onClassFilterAccepted() {
		try { SandboxWaiver.markAccepted(); }
		catch (IOException e) { LOGGER.error("Failed to write acceptance file", e); return; }
		LOGGER.info("Class filter acknowledged - disabling class filter");
		Common.getScriptManager().unloadAll();
	}

	private void onClassFilterDeclined() {
		StartupConfig.DISABLE_CLASS_FILTER.set(false);
		StartupConfig.DISABLE_CLASS_FILTER.save();
		StartupConfig.SPEC.save();
		LOGGER.info("User declined - keeping sandbox enabled");
		Common.getScriptManager().unloadAll();
	}

	private void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		FolderHash.hashFolderAsync(Common.BUNDLES)
			.thenAccept(hash -> ClientPacketDistributor.sendToServer(new BundleHashPacket(hash)))
			.exceptionally(e -> { LOGGER.error("Failed to hash bundles folder", e); return null; });
	}
}