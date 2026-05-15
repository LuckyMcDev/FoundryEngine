package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.*;
import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.cutscene.CutsceneItems;
import de.luckymcdev.foundryengine.common.cutscene.network.CutsceneActionPacket;
import de.luckymcdev.foundryengine.common.cutscene.network.CutsceneCommandPacket;
import de.luckymcdev.foundryengine.common.cutscene.network.CutscenePacket;
import de.luckymcdev.foundryengine.common.cutscene.network.ScreenEffectPacket;
import de.luckymcdev.foundryengine.common.cutscene.util.ServerCutsceneManager;
import de.luckymcdev.foundryengine.common.cutscene.util.ServerScreenEffectManager;
import de.luckymcdev.foundryengine.common.log.EngineLogAppender;
import de.luckymcdev.foundryengine.common.network.TestPacket;
import de.luckymcdev.foundryengine.common.network.packets.*;
import de.luckymcdev.foundryengine.common.network.packets.explorer.*;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import de.luckymcdev.foundryengine.common.scene.network.ScenePacket;
import de.luckymcdev.foundryengine.common.vpacks.BundleVirtualPacks;
import de.luckymcdev.foundryengine.common.vpacks.event.RegisterVirtualPackEvent;
import de.luckymcdev.foundryengine.common.world.entity.EngineEntities;
import de.luckymcdev.foundryengine.common.world.level.EngineLevels;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelConfig;
import de.luckymcdev.foundryengine.common.world.level.test.CustomLevel;
import de.luckymcdev.foundryengine.common.world.level.util.TransientChunkGenerator;
import de.luckymcdev.foundryengine.common.world.level.util.VoidChunkGenerator;
import de.luckymcdev.foundryengine.config.Config;
import de.luckymcdev.foundryengine.server.command.FoundryCommands;
import de.luckymcdev.foundryengine.server.packs.DynamicPackRepository;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeVersion;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

@Mod(Common.MODID)
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

        modBus.addListener(this::onRegisterEvent);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onConstruct);
        modBus.addListener(this::onAddPackFinders);
        modBus.addListener(this::onRegisterPayloadHandlers);
        modBus.addListener(CutsceneItems::onRegister);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::dedicatedServerSetup);
        modBus.addListener(this::postInit);

        BUS.addListener(this::onRegisterCommands);
        BUS.addListener(this::onServerAboutToStart);
        BUS.addListener(this::onServerStarted);
        BUS.addListener(this::onServerTick);
        BUS.addListener(this::onRegisterVirtualPacks);

        BUS.addListener(Common.getAreaManager()::onEntityTick);
        BUS.addListener(Common.getAreaManager()::onEntityRemoved);
        BUS.addListener(Common.getAreaManager()::onLevelLoad);
        BUS.addListener(Common.getAreaManager()::onServerStopping);
        BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                Common.getSavedDataManager().syncToPlayer(player);
            }
        });
        BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                Common.getSavedDataManager().syncToPlayer(player);
            }
        });

        registerSavedDataTypes();

        Config.registerCommon(modContainer);
        Config.registerStartup(modContainer);

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

    private void registerSavedDataTypes() {
        var manager = Common.getSavedDataManager();
        manager.register(Common.id("waypoints"),
                level -> de.luckymcdev.foundryengine.common.waypoint.storage.WaypointSavedData.get(level).getData(),
                null);
        manager.register(Common.id("areas"),
                level -> de.luckymcdev.foundryengine.common.area.AreaSavedData.get(level).toNbt(),
                null);
        manager.register(Common.id("scene_graph"),
                level -> de.luckymcdev.foundryengine.common.scene.storage.SceneSavedData.get(level).getData(),
                null);
        manager.register(Common.id("cutscene_manager"),
                level -> de.luckymcdev.foundryengine.common.cutscene.storage.CutsceneSavedData.get(level).getData(),
                null);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        BundleEvents.Internal.postCommonSetup(event);

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
        network.register(CutsceneActionPacket.DEFINITION);
        network.register(ScenePacket.DEFINITION);
        network.register(AreaPacket.DEFINITION);
        network.register(WaypointPacket.DEFINITION);
        network.register(SavedDataSyncPacket.DEFINITION);
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
        } catch (IOException e) {
            LOGGER.error("Error while loading bundles: {}", e.getLocalizedMessage());
        }

        Common.getBlueprintManager().executeCommonEvent(BlueprintEngine.BuiltinNodes.EVENT_BEGIN_PLAY.id);

        EngineLogAppender.Holder.addAppender();
    }

    private void onAddPackFinders(AddPackFindersEvent event) {
        PackType type = event.getPackType();
        event.addRepositorySource(new DynamicPackRepository(
                type,
                "foundry/bundles",
                "FoundryEngine: Bundles",
                () -> Common.getBundleManager().getBundles().stream()
                        .map(b -> type == PackType.CLIENT_RESOURCES
                                ? b.bundleFiles().assets()
                                : b.bundleFiles().data())
                        .filter(Files::exists)
                        .toList()
        ));
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        Common.getNetworkManager().handleRegistration(event);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        FoundryCommands.registerAll(event.getDispatcher(), event.getBuildContext());
    }

    private void onRegisterVirtualPacks(RegisterVirtualPackEvent.BeforeUser event) {
        BundleVirtualPacks.create().forEach(event::addPack);
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
        ServerCutsceneManager.tick();
        ServerScreenEffectManager.tick();
    }

    private void registerInternalEvents() {
        BUS.addListener(BlockEvents.Internal::postBroken);
        BUS.addListener(BlockEvents.Internal::postPlaced);
        BUS.addListener(BlockEvents.Internal::postLeftClicked);
        BUS.addListener(BlockEvents.Internal::postRightClicked);
        BUS.addListener(BlockEvents.Internal::postFarmlandTrampled);

        BUS.addListener(BundleEvents.Internal::postVanillaGame);
        BUS.addListener(BundleEvents.Internal::postServerAboutToStart);

        BUS.addListener(ClientEvents.Internal::postTick);
        BUS.addListener(ClientEvents.Internal::postStopped);
        BUS.addListener(ClientEvents.Internal::postStopping);
        BUS.addListener(ClientEvents.Internal::postChat);
        BUS.addListener(ClientEvents.Internal::postRenderGui);
        BUS.addListener(ClientEvents.Internal::postRenderGuiLayer);
        BUS.addListener(ClientEvents.Internal::postRenderHand);
        BUS.addListener(ClientEvents.Internal::postRenderAfterLevel);
        BUS.addListener(ClientEvents.Internal::postLoggedIn);
        BUS.addListener(ClientEvents.Internal::postLoggedOut);

        BUS.addListener(CommandEvents.Internal::post);
        BUS.addListener(CommandEvents.Internal::postClient);

        BUS.addListener(EntityEvents.Internal::postJoinLevel);
        BUS.addListener(EntityEvents.Internal::postDeath);
        BUS.addListener(EntityEvents.Internal::postDrops);
        BUS.addListener(EntityEvents.Internal::postHurt);
        BUS.addListener(EntityEvents.Internal::postSpawned);
        BUS.addListener(EntityEvents.Internal::postCheckSpawn);

        BUS.addListener(ItemEvents.Internal::postPickedUp);
        BUS.addListener(ItemEvents.Internal::postDestroyed);
        BUS.addListener(ItemEvents.Internal::postRightClicked);
        BUS.addListener(ItemEvents.Internal::postCrafted);
        BUS.addListener(ItemEvents.Internal::postDropped);
        BUS.addListener(ItemEvents.Internal::postFoodEaten);
        BUS.addListener(ItemEvents.Internal::postSmelted);
        BUS.addListener(ItemEvents.Internal::postDynamicTooltips);
        BUS.addListener(ItemEvents.Internal::postEntityInteracted);
        BUS.addListener(ItemEvents.Internal::postFirstLeftClicked);
        BUS.addListener(ItemEvents.Internal::postFirstRightClicked);

        BUS.addListener(LevelEvents.Internal::postLoad);
        BUS.addListener(LevelEvents.Internal::postUnload);
        BUS.addListener(LevelEvents.Internal::postSave);
        BUS.addListener(LevelEvents.Internal::postTick);
        BUS.addListener(LevelEvents.Internal::postBeforeExplosion);
        BUS.addListener(LevelEvents.Internal::postAfterExplosion);

        BUS.addListener(NetworkEvents.Internal::postLogin);
        BUS.addListener(NetworkEvents.Internal::postLogout);

        BUS.addListener(PlayerEvents.Internal::postLoggedIn);
        BUS.addListener(PlayerEvents.Internal::postLoggedOut);
        BUS.addListener(PlayerEvents.Internal::postTick);
        BUS.addListener(PlayerEvents.Internal::postChat);
        BUS.addListener(PlayerEvents.Internal::postAdvancement);
        BUS.addListener(PlayerEvents.Internal::postChestClosed);
        BUS.addListener(PlayerEvents.Internal::postChestOpened);
        BUS.addListener(PlayerEvents.Internal::postRespawned);
        BUS.addListener(PlayerEvents.Internal::postDecorateChat);

        BUS.addListener(RecipeEvents.Internal::postRecipesReceived);
        BUS.addListener(RecipeEvents.Internal::postModifyRecipes);

        BUS.addListener(ServerEvents.Internal::postAboutToStart);
        BUS.addListener(ServerEvents.Internal::postStarted);
        BUS.addListener(ServerEvents.Internal::postStarting);
        BUS.addListener(ServerEvents.Internal::postStopped);
        BUS.addListener(ServerEvents.Internal::postStopping);
        BUS.addListener(ServerEvents.Internal::postTick);
        BUS.addListener(ServerEvents.Internal::postTags);

        BUS.addListener(AreaEvents.Internal::postAreaEnter);
        BUS.addListener(AreaEvents.Internal::postAreaLeave);
        BUS.addListener(AreaEvents.Internal::postAreaTick);
    }
}