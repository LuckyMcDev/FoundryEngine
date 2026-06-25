package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.util.ServerScreenEffectManager;
import de.luckymcdev.foundryengine.common.data.BundleDataGenerator;
import de.luckymcdev.foundryengine.common.event.*;
import de.luckymcdev.foundryengine.common.event.modification.BlockModificationEvent;
import de.luckymcdev.foundryengine.common.event.modification.ItemModificationEvent;
import de.luckymcdev.foundryengine.common.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.log.EngineLogAppender;
import de.luckymcdev.foundryengine.common.network.packets.BundleHashPacket;
import de.luckymcdev.foundryengine.common.network.packets.TestPacket;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.ClientboundDialoguePacket;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.DialogueSavePacket;
import de.luckymcdev.foundryengine.common.network.packets.dialogue.ServerboundDialoguePacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.*;
import de.luckymcdev.foundryengine.common.network.packets.explorer.*;
import de.luckymcdev.foundryengine.common.network.packets.sync.SavedDataSyncPacket;
import de.luckymcdev.foundryengine.common.network.packets.sync.ScreenEffectPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundChangeWeatherPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundSetTimePacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundSpawnEntityPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import de.luckymcdev.foundryengine.common.world.level.EngineLevels;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelConfig;
import de.luckymcdev.foundryengine.common.world.level.test.CustomLevel;
import de.luckymcdev.foundryengine.common.world.level.util.TransientChunkGenerator;
import de.luckymcdev.foundryengine.common.world.level.util.VoidChunkGenerator;
import de.luckymcdev.foundryengine.config.Config;
import de.luckymcdev.foundryengine.config.StartupConfig;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeVersion;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
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
        registerSavedDataTypes();

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
    public static IEventBus getModBus() {
        return modBus;
    }

    private void registerModBus(IEventBus modBus) {
        Common.getGameStageHandler().register(modBus);
        EngineRegistries.register(modBus);
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
        AreaEvents.Internal.register(BUS);
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

        BUS.addListener(this::onLevelLoad);
        BUS.addListener(this::onLevelTick);

        BUS.addListener(this::onPlayerDisconnect);
        BUS.addListener(this::onPlayerChangedDimension);
    }

    private void registerSavedDataTypes() {
        var manager = Common.getSavedDataManager();
        manager.register(Common.id("waypoints"), level -> Common.getWaypointManager().toNbt(level));
        manager.register(Common.id("areas"), level -> Common.getAreaManager().toNbt(level));
        manager.register(Common.id("cutscene_manager"), level -> Common.getCutsceneManager().toNbt(level));
        manager.register(Common.id("dialogue"), level -> Common.getDialogueManager().toNbt());
    }

    private void onRegisterEvent(RegisterEvent event) {
        event.register(Registries.CHUNK_GENERATOR, helper -> {
            helper.register(Common.id("void"), VoidChunkGenerator.CODEC);
            helper.register(Common.id("transient"), TransientChunkGenerator.CODEC);
        });
    }

    private void onConstruct(FMLConstructModEvent event) {
        try {
            Common.getBundleManager().discover(Common.BUNDLES);
            if (modBus != null) {
                modBus.addListener((RegisterEvent ev) -> {
                    RegistryEvent registryEvent = new RegistryEvent(ev, modBus);
                    ModLoader.postEvent(registryEvent);
                    BundleEvents.Internal.postRegistry(registryEvent);
                });
            }
            BundleDataGenerator.runAll();
        } catch (IOException e) {
            LOGGER.error("Error while loading bundles: {}", e.getLocalizedMessage());
        }

        EngineLogAppender.Holder.addAppender();
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        BundleEvents.Internal.postCommonSetup(event);
        BundleDataGenerator.runAll();

        var network = Common.getNetworkManager();
        network.register(TestPacket.DEFINITION);
        network.register(ServerBoundSetTimePacket.DEFINITION);
        network.register(ServerBoundChangeWeatherPacket.DEFINITION);
        network.register(ClientBoundFileListPacket.DEFINITION);
        network.register(ClientBoundFileContentPacket.DEFINITION);
        network.register(ServerBoundRequestFileListPacket.DEFINITION);
        network.register(ServerBoundRequestFileContentPacket.DEFINITION);
        network.register(ServerBoundSaveFilePacket.DEFINITION);
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
        var bundlesGeneratedRepo = new DynamicPackRepository(
                type,
                "foundryengine/bundles_generated",
                "FoundryEngine: Generated",
                "Foundry Engine Generated Resource Files",
                () -> {
                    Path path = type == PackType.CLIENT_RESOURCES
                            ? BundleDataGenerator.getGeneratedAssetsPath()
                            : BundleDataGenerator.getGeneratedDataPath();
                    return Files.exists(path) ? List.of(path) : List.of();
                },
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
        Common.getBundleManager().loadServerScripts();
    }

    private void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        Common.getDialogueManager().loadFrom(server.overworld());
        EngineLevels.get(server).openTemporaryLevel(
                new RuntimeLevelConfig()
                        .setGenerator(server.overworld().getChunkSource().getGenerator())
                        .setLevelConstructor(CustomLevel::new)
                        .setSeed("North Carolina".hashCode())
                        .setMirrorOverworldGameRules(true)
        );

        event.getServer().getPlayerList().getPlayers().forEach(player -> {
            Common.getSavedDataManager().syncToPlayer(player);
        });
    }

    private void onServerStopping(ServerStoppingEvent event) {
        Common.getGameManager().stopAll();
        Common.getWaypointManager().onServerStopping(event);
        Common.getBundleManager().setServer(null);
        var overworld = event.getServer().overworld();
        Common.getDialogueManager().saveTo(overworld);
    }

    private void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        Common.getCutsceneSessionManager().tick(server);
        Common.getGameStageHandler().onPlayerTick(event);
        ServerScreenEffectManager.tick();
        for (var level : server.getAllLevels()) {
            Common.getGameManager().tickServer(server, level);
        }
    }

    private void onLevelLoad(LevelEvent.Load event) {
        Common.getCutsceneManager().onLevelLoad(event);
        if (event.getLevel() instanceof Level level) {
            Common.getWaypointManager().onLevelLoad(level);
        }
    }

    private void onLevelTick(LevelTickEvent.Post event) {
        Common.getGameManager().tickCommon(event.getLevel());
    }

    private void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Common.getDialogueManager().onPlayerDisconnect(player);
        }
    }

    private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Common.getSavedDataManager().syncToPlayer(player);
        }
    }
}