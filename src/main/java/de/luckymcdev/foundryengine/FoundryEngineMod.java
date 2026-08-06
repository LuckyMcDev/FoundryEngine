package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.util.ServerScreenEffectManager;
import de.luckymcdev.foundryengine.common.data.gen.BundleDataGenerator;
import de.luckymcdev.foundryengine.common.event.BlockEvents;
import de.luckymcdev.foundryengine.common.event.BundleEvents;
import de.luckymcdev.foundryengine.common.event.ClientEvents;
import de.luckymcdev.foundryengine.common.event.CommandEvents;
import de.luckymcdev.foundryengine.common.event.DialogueEvents;
import de.luckymcdev.foundryengine.common.event.EntityEvents;
import de.luckymcdev.foundryengine.common.event.GameEvents;
import de.luckymcdev.foundryengine.common.event.ItemEvents;
import de.luckymcdev.foundryengine.common.event.LevelEvents;
import de.luckymcdev.foundryengine.common.event.NetworkEvents;
import de.luckymcdev.foundryengine.common.event.PlayerEvents;
import de.luckymcdev.foundryengine.common.event.RecipeEvents;
import de.luckymcdev.foundryengine.common.event.ServerEvents;
import de.luckymcdev.foundryengine.common.event.SlotEvents;
import de.luckymcdev.foundryengine.common.event.StageEvents;
import de.luckymcdev.foundryengine.common.event.modification.BlockModificationEvent;
import de.luckymcdev.foundryengine.common.event.modification.ItemModificationEvent;
import de.luckymcdev.foundryengine.common.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.log.EngineLogAppender;
import de.luckymcdev.foundryengine.common.network.packets.BundleHashPacket;
import de.luckymcdev.foundryengine.common.network.packets.CustomDataPacket;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.ClientboundDialoguePacket;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.DialogueSavePacket;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.ServerboundDialoguePacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.AreaPacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.CutsceneCommandPacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.CutscenePacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.GiveItemPacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.LinearizeCutscenePacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.WaypointPacket;
import de.luckymcdev.foundryengine.common.network.packets.explorer.ClientBoundExplorerPacket;
import de.luckymcdev.foundryengine.common.network.packets.explorer.ServerBoundExplorerPacket;
import de.luckymcdev.foundryengine.common.network.packets.sync.SavedDataSyncPacket;
import de.luckymcdev.foundryengine.common.network.packets.sync.ScreenEffectPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundChangeWeatherPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundSetTimePacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundSpawnEntityPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.registry.RegistryCollector;
import de.luckymcdev.foundryengine.common.world.level.util.TransientChunkGenerator;
import de.luckymcdev.foundryengine.common.world.level.util.VoidChunkGenerator;
import de.luckymcdev.foundryengine.config.Config;
import de.luckymcdev.foundryengine.config.StartupConfig;
import de.luckymcdev.foundryengine.mixin.MinecraftServerAccess;
import de.luckymcdev.foundryengine.server.command.FoundryCommands;
import de.luckymcdev.foundryengine.server.packs.DynamicPackRepository;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.CrashReportCallables;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeVersion;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Main entrypoint for FoundryEngine. Registers all event bus listeners, packets, and subsystems.
 */
@Mod(value = Common.MODID)
public class FoundryEngineMod {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final IEventBus BUS = NeoForge.EVENT_BUS;
	public static @Nullable ArtifactVersion modVersion;
	private static @Nullable IEventBus modBus;

	public FoundryEngineMod(IEventBus modBus, ModContainer modContainer) {
		FoundryEngineMod.modBus = modBus;
		FoundryEngineMod.modVersion = modContainer.getModInfo().getVersion();

		registerModBus(modBus);
		registerInternalEvents();
		registerModEventHandlers(modBus);
		registerNeoForgeEventHandlers();
		registerCrashReportCallables();

		Config.registerCommon(modContainer);
		Config.registerStartup(modContainer);

		if (StartupConfig.CLEAR_DATA_CACHE.get()) {
			try {
				FileUtils.deleteDirectory(BundleDataGenerator.OUTPUT_ROOT.toFile());
			} catch (IOException e) {
				LOGGER.error("Could not clear Data Cache.");
			}
			StartupConfig.CLEAR_DATA_CACHE.set(false);
		}

		LOGGER.info("""
				
				███████╗███████╗
				██╔════╝██╔════╝  Foundry Engine {}
				█████╗  █████╗    Running on NeoForge {}
				██╔══╝  ██╔══╝    Minecraft {}
				██║     ███████╗  Platform {}
				╚═╝     ╚══════╝""",
			modVersion,
			NeoForgeVersion.getVersion(),
			SharedConstants.getCurrentVersion().name(),
			Util.getPlatform().name());
	}

	/**
	 * Returns the mod event bus for internal use.
	 */
	@ApiStatus.Internal
	public static @Nullable IEventBus getModBus() {
		return modBus;
	}

	private void registerModBus(IEventBus modBus) {
		Common.getGameStageHandler().register(modBus);
		ClientEvents.Internal.registerModBus(modBus);
	}

	private void registerInternalEvents() {
		BlockEvents.Internal.register(BUS);
		BundleEvents.Internal.register(BUS);
		ClientEvents.Internal.register(BUS);
		CommandEvents.Internal.register(BUS);
		EntityEvents.Internal.register(BUS);
		ItemEvents.Internal.register(BUS);
		LevelEvents.Internal.register(BUS);
		NetworkEvents.Internal.register(BUS);
		PlayerEvents.Internal.register(BUS);
		RecipeEvents.Internal.register(BUS);
		ServerEvents.Internal.register(BUS);
		StageEvents.Internal.register(BUS);
		GameEvents.Internal.register(BUS);
		SlotEvents.Internal.register(BUS);
		DialogueEvents.Internal.register(BUS);
	}

	private void registerModEventHandlers(IEventBus modBus) {
		modBus.addListener(EventPriority.LOWEST, this::onRegisterEvent);
		modBus.addListener(this::onCommonSetup);
		modBus.addListener(this::onConstruct);
		modBus.addListener(this::onAddPackFinders);
		modBus.addListener(this::onRegisterPayloadHandlers);
		modBus.addListener(this::onClientSetup);
		modBus.addListener(this::onDedicatedServerSetup);
		modBus.addListener(this::onPostInit);
		modBus.addListener(EventPriority.LOWEST, this::onLoadComplete);
		modBus.addListener(this::onItemModification);
		modBus.addListener(this::onEngineRegister);
		modBus.addListener(Config::onLoad);
		modBus.addListener(Config::onReload);
	}

	private void registerNeoForgeEventHandlers() {
		BUS.addListener(this::onRegisterCommands);
		BUS.addListener(this::onServerAboutToStart);
		BUS.addListener(this::onServerStarting);
		BUS.addListener(this::onServerStarted);
		BUS.addListener(this::onServerStopping);
		BUS.addListener(this::onServerTick);

		BUS.addListener(this::onLevelTick);

		BUS.addListener(Common.getAreaManager()::onBlockBreak);
		BUS.addListener(Common.getAreaManager()::onBlockPlace);

		var stages = Common.getGameStageHandler();
		BUS.register(stages.blocks());
		BUS.register(stages.dimensions());
		BUS.register(stages.item());
		BUS.register(stages.loot());
		BUS.register(stages.mobs());
		BUS.register(stages.recipes());

		BUS.addListener(this::onPlayerDisconnect);
		BUS.addListener(this::onPlayerChangedDimension);
	}

	private void registerCrashReportCallables() {
		CrashReportCallables.registerCrashCallable("FoundryEngine Bundles", () -> {
			var bundles = Common.getBundleManager().getBundles();
			if (bundles.isEmpty()) {
				return "None";
			}
			var sb = new StringBuilder("\n");
			for (var bundle : bundles) {
				var info = bundle.info();
				sb.append("\t\t- ").append(info.id()).append(" v").append(info.versionInfo());
				if (!info.authors().isEmpty()) {
					sb.append(" by ").append(String.join(", ", info.authors()));
				}
				sb.append('\n');
			}
			return sb.toString().stripTrailing();
		}, () -> Common.getBundleManager().anyBundles());

		CrashReportCallables.registerCrashCallable("FoundryEngine Game Sessions", () -> {
			var sessions = Common.getGameManager().getAllSessions();
			if (sessions.isEmpty()) {
				return "None";
			}
			var sb = new StringBuilder("\n");
			for (var session : sessions) {
				sb.append("\t\t- ").append(session.id()).append('\n');
			}
			return sb.toString().stripTrailing();
		}, () -> Common.getGameManager().anySession());
	}

	private void onRegisterEvent(RegisterEvent event) {
		event.register(Registries.CHUNK_GENERATOR, helper -> {
			helper.register(Common.id("void"), VoidChunkGenerator.CODEC);
			helper.register(Common.id("transient"), TransientChunkGenerator.CODEC);
		});
	}

	private void onEngineRegister(RegisterEvent event) {
		if (modBus == null) {
			return;
		}
		RegistryCollector collector = new RegistryCollector();
		Common.setRegistryCollector(collector);
		RegistryEvent registryEvent = new RegistryEvent(event, collector);
		modBus.post(registryEvent);
		//ModLoader.postEventWrapContainerInModOrder(registryEvent);
		BundleEvents.Internal.postRegistry(registryEvent);
	}

	private void onConstruct(FMLConstructModEvent event) {
		try {
			Common.getBundleManager().discover(Common.BUNDLES);
			Common.getBundleManager().refreshModList();
		} catch (IOException e) {
			LOGGER.error("Error while loading bundles: {}", (Object) e.getStackTrace());
		}

		EngineLogAppender.Holder.addAppender();
	}

	private void onCommonSetup(FMLCommonSetupEvent event) {
		BundleEvents.Internal.postCommonSetup(event);
		BundleDataGenerator.runAll();

		var network = Common.getNetworkManager();
		network.register(ServerBoundSetTimePacket.DEFINITION);
		network.register(ServerBoundChangeWeatherPacket.DEFINITION);
		network.register(ServerBoundExplorerPacket.DEFINITION);
		network.register(ClientBoundExplorerPacket.DEFINITION);
		network.register(ServerBoundTeleportPacket.DEFINITION);
		network.register(ServerBoundSpawnEntityPacket.DEFINITION);
		network.register(BundleHashPacket.DEFINITION);
		network.register(CutscenePacket.DEFINITION);
		network.register(ScreenEffectPacket.DEFINITION);
		network.register(CutsceneCommandPacket.DEFINITION);
		network.register(GiveItemPacket.DEFINITION);
		network.register(LinearizeCutscenePacket.DEFINITION);
		network.register(AreaPacket.DEFINITION);
		network.register(WaypointPacket.DEFINITION);
		network.register(SavedDataSyncPacket.DEFINITION);
		network.register(ClientboundDialoguePacket.DEFINITION);
		network.register(ServerboundDialoguePacket.DEFINITION);
		network.register(DialogueSavePacket.DEFINITION);
		network.register(CustomDataPacket.DEFINITION);
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		BundleEvents.Internal.postClientSetup(event);
	}

	private void onDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
		BundleEvents.Internal.postDedicatedServerSetup(event);
	}

	private void onPostInit(InterModProcessEvent event) {
		BundleEvents.Internal.postPostInit(event);
	}

	private void onAddPackFinders(AddPackFindersEvent event) {
		PackType type = event.getPackType();
		var bundlesRepo = new DynamicPackRepository(
			type,
			"foundryengine/bundles",
			"FoundryEngine: Bundles",
			"Foundry Engine Bundle Resource Files",
			() -> Common.getBundleManager().getBundles().stream()
				.map(b -> type == PackType.CLIENT_RESOURCES
					? b.bundleFiles().assets()
					: b.bundleFiles().data())
				.filter(Files::exists)
				.toList(),
			Pack.Position.TOP,
			false
		);
		Path generatedPath = type == PackType.CLIENT_RESOURCES
			? BundleDataGenerator.getGeneratedAssetsPath()
			: BundleDataGenerator.getGeneratedDataPath();
		try {
			Files.createDirectories(generatedPath);
		} catch (IOException ignored) {
		}
		var bundlesGeneratedRepo = new DynamicPackRepository(
			type,
			"foundryengine/bundles_generated",
			"FoundryEngine: Generated",
			"Foundry Engine Generated Resource Files",
			() -> List.of(generatedPath),
			Pack.Position.BOTTOM,
			true
		);
		event.addRepositorySource(bundlesRepo);
		event.addRepositorySource(bundlesGeneratedRepo);
	}

	private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
		Common.getNetworkManager().handleRegistration(event);
	}

	private void onLoadComplete(FMLLoadCompleteEvent event) {
		event.enqueueWork(() -> {
			for (Block block : BuiltInRegistries.BLOCK) {
				var e = new BlockModificationEvent(block);
				BUS.post(e);
			}
		});
	}

	private void onItemModification(ModifyDefaultComponentsEvent event) {
		ItemModificationEvent.bind(event);
		for (Item item : BuiltInRegistries.ITEM) {
			var e = new ItemModificationEvent(item);
			BUS.post(e);
		}
		ItemModificationEvent.flush();
	}

	private void onRegisterCommands(RegisterCommandsEvent event) {
		FoundryCommands.registerAll(event.getDispatcher(), event.getBuildContext());
	}

	private void onServerAboutToStart(ServerAboutToStartEvent event) {
		Common.getBundleManager().setServer(event.getServer());
	}

	private void onServerStarting(ServerStartingEvent event) {
		Common.getSavedDataManager().load();
		Common.getWaypointManager().load();
		Common.getAreaManager().load();
		Common.getCutsceneManager().load();
		Common.getDialogueManager().load();
		Common.getBundleManager().loadServerScripts();
	}

	private void onServerStarted(ServerStartedEvent event) {
		var server = event.getServer();
		String worldName = server.getWorldData().getLevelName();
		LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerAccess) server).getStorageSource();
		Path worldDataPath = storageAccess.getLevelDirectory().path().resolve("foundryengine").resolve("game");
		Common.getGameManager().setWorldDataPath(worldName, worldDataPath);
		Common.getGameManager().autoStartAll(worldName);
		Common.getSavedDataManager().syncToAll();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		Common.getGameManager().stopAll();
		Common.getWaypointManager().save();
		Common.getAreaManager().save();
		Common.getCutsceneManager().save();
		Common.getDialogueManager().save();
		Common.getSavedDataManager().save();
		Common.getBundleManager().setServer(null);
	}

	private void onServerTick(ServerTickEvent.Post event) {
		var server = event.getServer();
		Common.getGameManager().beginServerTick();
		Common.getCutsceneSessionManager().tick(server);
		Common.getGameStageHandler().onPlayerTick(event);
		ServerScreenEffectManager.tick();
		for (var level : server.getAllLevels()) {
			Common.getGameManager().tickServer(server, level);
		}
	}

	private void onLevelTick(LevelTickEvent.Post event) {
		Common.getGameManager().tickCommon(event.getLevel());
		Common.getAreaManager().onLevelTick(event);
	}

	private void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			Common.getCutsceneSessionManager().onPlayerDisconnect(player);
			ServerScreenEffectManager.onPlayerDisconnect(player);
			Common.getDialogueManager().onPlayerDisconnect(player);
		}
	}

	private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			Common.getSavedDataManager().syncToAll();
		}
	}
}
