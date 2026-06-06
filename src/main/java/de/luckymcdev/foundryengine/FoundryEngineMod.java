package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.*;
import de.luckymcdev.foundryengine.api.event.modification.BlockModificationEvent;
import de.luckymcdev.foundryengine.api.event.modification.ItemModificationEvent;
import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.cutscene.util.ServerScreenEffectManager;
import de.luckymcdev.foundryengine.common.data.BundleDataGenerator;
import de.luckymcdev.foundryengine.common.item.ModItems;
import de.luckymcdev.foundryengine.common.log.EngineLogAppender;
import de.luckymcdev.foundryengine.common.network.packets.BundleHashPacket;
import de.luckymcdev.foundryengine.common.network.packets.TestPacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.AreaPacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.CutsceneCommandPacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.CutscenePacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.WaypointPacket;
import de.luckymcdev.foundryengine.common.network.packets.explorer.*;
import de.luckymcdev.foundryengine.common.network.packets.sync.SavedDataSyncPacket;
import de.luckymcdev.foundryengine.common.network.packets.sync.ScreenEffectPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundChangeWeatherPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundSetTimePacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundSpawnEntityPacket;
import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import de.luckymcdev.foundryengine.common.world.entity.EngineEntities;
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
import net.minecraft.client.Minecraft;
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
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeVersion;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
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

        modBus.addListener(EventPriority.LOWEST, this::onRegisterEvent);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onConstruct);
        modBus.addListener(this::onAddPackFinders);
        modBus.addListener(this::onRegisterPayloadHandlers);
        modBus.addListener(ModItems::onRegister);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::dedicatedServerSetup);
        modBus.addListener(this::postInit);
        modBus.addListener(Config::onLoad);
        modBus.addListener(Config::onReload);

        BUS.addListener(this::onRegisterCommands);
        BUS.addListener(this::onServerAboutToStart);
        BUS.addListener(this::onServerStarted);
        BUS.addListener(this::onServerTick);
        BUS.addListener(Common.getAreaManager()::onLevelTick);
        BUS.addListener(Common.getAreaManager()::onLevelLoad);
        BUS.addListener(Common.getAreaManager()::onServerStopping);
        BUS.addListener(Common.getCutsceneManager()::onLevelLoad);
        BUS.addListener(this::onWaypointLevelLoad);
        BUS.addListener(this::onWaypointServerStopping);
        BUS.addListener(this::onPlayerLoggedIn);
        BUS.addListener(this::onPlayerChangedDimension);

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

    @ApiStatus.Internal
    public static IEventBus getModBus() {
        return modBus;
    }

    private void registerModBus(IEventBus modBus) {
        Common.getGameStageHandler().register(modBus);
        EngineRegistries.register(modBus);
        EngineEntities.register(modBus);
    }

    private void onWaypointLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof Level level) {
            Common.getWaypointManager().onLevelLoad(level);
        }
    }

    private void onWaypointServerStopping(ServerStoppingEvent event) {
        Common.getWaypointManager().onServerStopping(event);
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Common.getSavedDataManager().syncToPlayer(player);
        }
    }

    private void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Common.getSavedDataManager().syncToPlayer(player);
        }
    }

    private void registerSavedDataTypes() {
        var manager = Common.getSavedDataManager();
        manager.register(Common.id("waypoints"), level -> Common.getWaypointManager().toNbt(level),
                null);
        manager.register(Common.id("areas"), level -> Common.getAreaManager().toNbt(level),
                null);
        manager.register(Common.id("cutscene_manager"), level -> Common.getCutsceneManager().toNbt(level),
                null);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        BundleEvents.Internal.postCommonSetup(event);
        BundleDataGenerator.runAll();

        if (FMLEnvironment.getDist().isClient()) {
            event.enqueueWork(() -> Minecraft.getInstance().reloadResourcePacks());
        }

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
        network.register(AreaPacket.DEFINITION);
        network.register(WaypointPacket.DEFINITION);
        network.register(SavedDataSyncPacket.DEFINITION);

        event.enqueueWork(() -> {
            for (Block block : BuiltInRegistries.BLOCK) {
                BUS.post(new BlockModificationEvent(block));
            }
            for (Item block : BuiltInRegistries.ITEM) {
                BUS.post(new ItemModificationEvent(block));
            }
        });
    }

    private void clientSetup(FMLClientSetupEvent event) {
        BundleEvents.Internal.postClientSetup(event);
    }

    private void dedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
        BundleEvents.Internal.postDedicatedServerSetup(event);
    }

    private void postInit(InterModProcessEvent event) {
        BundleEvents.Internal.postPostInit(event);
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

        Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_BEGIN_PLAY.id);

        EngineLogAppender.Holder.addAppender();
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

    private void onRegisterCommands(RegisterCommandsEvent event) {
        FoundryCommands.registerAll(event.getDispatcher(), event.getBuildContext());
    }

    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        Common.getBundleManager().loadServerScripts();
    }

    private void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        EngineLevels.get(server).openTemporaryLevel(
                new RuntimeLevelConfig()
                        .setGenerator(server.overworld().getChunkSource().getGenerator())
                        .setLevelConstructor(CustomLevel::new)
                        .setSeed("North Carolina".hashCode())
                        .setMirrorOverworldGameRules(true)
        );
    }

    private void onServerTick(ServerTickEvent.Post event) {
        Common.getCutsceneSessionManager().tick(event.getServer());
        ServerScreenEffectManager.tick();
    }

    private void registerInternalEvents() {
        AreaEvents.Internal.register(BUS);
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
    }
}
