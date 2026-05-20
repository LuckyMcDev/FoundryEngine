package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.command.FoundryCommandsClient;
import de.luckymcdev.foundryengine.client.debug.screen.BundleDebugEntry;
import de.luckymcdev.foundryengine.client.debug.screen.GameStagesDebugEntry;
import de.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent;
import de.luckymcdev.foundryengine.client.editor.panel.TestPanel;
import de.luckymcdev.foundryengine.client.editor.panel.cutscenes.CutscenePanel;
import de.luckymcdev.foundryengine.client.editor.panel.cutscenes.CutsceneTimelinePanel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.AreaPanel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.BlueprintsPanel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.MainEditor;
import de.luckymcdev.foundryengine.client.editor.panel.explorer.FileExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.panel.explorer.ResourceExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.*;
import de.luckymcdev.foundryengine.client.editor.panel.view.InfoPanel;
import de.luckymcdev.foundryengine.client.editor.panel.view.ThemeSelectorPanel;
import de.luckymcdev.foundryengine.client.event.RegisterRenderingStuffEvent;
import de.luckymcdev.foundryengine.client.ext.ModPathBroadcaster;
import de.luckymcdev.foundryengine.client.icons.ScreenIconExporter;
import de.luckymcdev.foundryengine.client.render.EngineRenderPipelines;
import de.luckymcdev.foundryengine.client.render.WorldViewMatrix;
import de.luckymcdev.foundryengine.client.render.entity.EngineEntityRenderers;
import de.luckymcdev.foundryengine.client.render.obj.ObjModel;
import de.luckymcdev.foundryengine.client.util.key.RegisterKeyBindingEvent;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.BundleHashPacket;
import de.luckymcdev.foundryengine.common.network.packets.editor.WaypointPacket;
import de.luckymcdev.foundryengine.common.util.FolderHash;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.config.ClientConfig;
import de.luckymcdev.foundryengine.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(value = Common.MODID, dist = Dist.CLIENT)
public class FoundryEngineModClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final IEventBus BUS = NeoForge.EVENT_BUS;
    private static final ObjModel SUZANNE = new ObjModel(Common.id("obj/suzanne.obj"));
    private boolean hasIconAutoExported = false;

    public FoundryEngineModClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::addClientReloadListener);
        modBus.addListener(this::onRegisterKeyMapping);
        modBus.addListener(this::onRegisterDebugEntry);
        modBus.addListener(this::onRegisterDebugRenderers);
        modBus.addListener(EngineEntityRenderers::onRegisterRenderers);

        BUS.addListener(this::onRegisterKeyBinding);
        BUS.addListener(this::onRegisterPanels);
        BUS.addListener(this::onClientTick);
        BUS.addListener(this::onRenderLevel);
        BUS.addListener(this::onRegisterCommands);
        BUS.addListener(this::onLoggingIn);

        Config.registerClient(modContainer);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.debug("FoundryEngineModClient setup called");
        ModPathBroadcaster.onClientSetup();
        Common.getBundleManager().loadClientScripts();
        event.enqueueWork(() -> {
            BUS.post(new RegisterRenderingStuffEvent(Client.getResourceManager()));
            BUS.post(new RegisterPanelEvent());
        });
        Client.getObjModelManager().registerObjModel(SUZANNE);
        registerClientSyncHandlers();
    }

    private void registerClientSyncHandlers() {
        var savedataManager = Common.getSavedDataManager();
        savedataManager.registerClientHandler(Common.id("waypoints"), data -> {
            var dimension = Client.getMc().level != null ? Client.getMc().level.dimension() : null;
            if (dimension == null) return;
            Common.getWaypointManager().applySync(dimension, data);
        });
        savedataManager.registerClientHandler(Common.id("areas"), data -> {
            var dimension = Client.getMc().level != null ? Client.getMc().level.dimension() : null;
            if (dimension == null) return;
            Common.getAreaManager().applySync(dimension, data);
        });
        savedataManager.registerClientHandler(Common.id("cutscene_manager"), data -> {
            var dimension = Client.getMc().level != null ? Client.getMc().level.dimension() : null;
            if (dimension == null) return;
            Common.getCutsceneManager().applySync(dimension, data);
        });
    }

    private void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        try {
            String hash = FolderHash.hashFolder(Common.BUNDLES);
            ClientPacketDistributor.sendToServer(new BundleHashPacket(hash));
        } catch (Exception e) {
            LOGGER.error("Failed to hash bundles folder", e);
        }
    }

    private void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        BUS.post(new RegisterKeyBindingEvent(Client.getKeyBindingManager()));
        Client.getKeyBindingManager().getKeyBindings().forEach(kb -> event.register(kb.mapping()));
        event.registerCategory(Client.EDITOR_CATEGORY);
        event.register(Client.PRIMARY_WAYPOINT_KEY);
        event.register(Client.SECONDARY_WAYPOINT_KEY);
        event.register(Client.REMOVE_WAYPOINT_KEY);
        event.register(Client.CLEAR_WAYPOINTS_KEY);
    }

    private void onRegisterKeyBinding(RegisterKeyBindingEvent event) {
        event.register(Client.EDITOR_KEY);
        event.register(Client.MENU_BAR_KEY);
    }

    private void onRegisterCommands(RegisterClientCommandsEvent event) {
        FoundryCommandsClient.registerAll(event.getDispatcher(), event.getBuildContext());
    }

    private void onRegisterDebugEntry(RegisterDebugEntriesEvent event) {
        event.register(Common.id("bundles_info"), new BundleDebugEntry(Common.getBundleManager()));
        event.register(Common.id("gamestages_info"), new GameStagesDebugEntry());
    }

    private void onRegisterDebugRenderers(RegisterDebugRenderersEvent event) {
    }

    private void onRegisterPanels(RegisterPanelEvent event) {
        event.register(TestPanel.INSTANCE);
        event.register(FileExplorerPanel.INSTANCE);
        event.register(ResourceExplorerPanel.INSTANCE);
        event.register(ConsolePanel.INSTANCE);
        event.register(MainEditor.INSTANCE);
        event.register(MinecraftToolsPanel.INSTANCE);
        event.register(StopwatchPanel.INSTANCE);
        event.register(InfoPanel.INSTANCE);
        event.register(CataloguePanel.INSTANCE);
        event.register(ThemeSelectorPanel.INSTANCE);
        event.register(EffectPanel.INSTANCE);
        event.register(BlueprintsPanel.INSTANCE);
        event.register(AreaPanel.INSTANCE);
        event.register(CutscenePanel.INSTANCE);
        event.register(CutsceneTimelinePanel.INSTANCE);
        event.register(WaypointPanel.INSTANCE);
    }

    private void addClientReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(Common.id("imgui_handler"), (ResourceManagerReloadListener) Client.getImGuiManager());
        event.addListener(Common.id("obj_models"), (sharedState, backgroundExecutor, barrier, gameExecutor) ->
                CompletableFuture
                        .<Void>supplyAsync(() -> null, backgroundExecutor)
                        .thenCompose(barrier::wait)
                        .thenAcceptAsync(v ->
                                        Client.getObjModelManager().loadModels(),
                                gameExecutor
                        )
        );
    }

    private void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
        var camState = event.getLevelRenderState().cameraRenderState;
        Client.updateMain(camState.viewRotationMatrix, camState.projectionMatrix);

        Client.getCutsceneManager().renderTick();
        Client.getCutsceneScreenEffectManager().renderTick();
        Client.getEditorController().renderFeatures();
        Client.getWaypointRenderer().renderWaypoints(event);

        Matrix4f modelView = WorldViewMatrix.from(event).at(0, 110, 0).scale(2).buildModelView();
        SUZANNE.renderModel(modelView, EngineRenderPipelines.POSITION_COLOR_NORMAL);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Client.getEditorManager().handleTick();
        Client.getCutsceneManager().clientTick();
        Client.getEditorController().clientTick();
        handleWaypointKeys();

        if (!hasIconAutoExported && ClientConfig.AUTO_EXPORT.get()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.screen != null) return;

            hasIconAutoExported = true;
            LOGGER.info("Auto-export: Initializing icon generation...");

            ScreenIconExporter screen = new ScreenIconExporter(
                    mc.level.registryAccess(),
                    mc.getWindow().getGuiScale(),
                    null,
                    false
            );

            if (screen.hasWork()) {
                mc.setScreen(screen);
            } else {
                LOGGER.info("Auto-export: All icons are up to date.");
            }
        }
    }

    private void handleWaypointKeys() {
        Vec3i targetedCoords = Client.getHitOrNull();

        while (Client.PRIMARY_WAYPOINT_KEY.consumeClick()) {
            if (targetedCoords != null) {
                ClientPacketDistributor.sendToServer(WaypointPacket.add(
                        targetedCoords.getX(), targetedCoords.getY(), targetedCoords.getZ(),
                        "Info", "I", Color.TURQUOISE.argb()
                ));
            }
        }

        while (Client.SECONDARY_WAYPOINT_KEY.consumeClick()) {
            if (targetedCoords != null) {
                ClientPacketDistributor.sendToServer(WaypointPacket.add(
                        targetedCoords.getX(), targetedCoords.getY(), targetedCoords.getZ(),
                        "Warn", "W", Color.ORANGE.argb()
                ));
            }
        }

        while (Client.REMOVE_WAYPOINT_KEY.consumeClick()) {
            if (targetedCoords != null) {
                ClientPacketDistributor.sendToServer(WaypointPacket.remove(
                        targetedCoords.getX(), targetedCoords.getY(), targetedCoords.getZ()
                ));
            }
        }

        while (Client.CLEAR_WAYPOINTS_KEY.consumeClick()) {
            ClientPacketDistributor.sendToServer(WaypointPacket.clear());
        }
    }
}
