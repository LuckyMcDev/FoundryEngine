package de.luckymcdev.foundryengine.client;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.client.area.AreaRenderer;
import de.luckymcdev.foundryengine.client.command.FoundryCommandsClient;
import de.luckymcdev.foundryengine.client.cutscene.ClientCutsceneManager;
import de.luckymcdev.foundryengine.client.cutscene.ClientScreenEffectManager;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneEditor;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneRenderer;
import de.luckymcdev.foundryengine.client.debug.screen.BundleDebugEntry;
import de.luckymcdev.foundryengine.client.debug.screen.GameStagesDebugEntry;
import de.luckymcdev.foundryengine.client.editor.builtin.MainEditor;
import de.luckymcdev.foundryengine.client.editor.builtin.TestPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.area.AreaPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.blueprint.BlueprintsPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.cutscene.CutscenePanel;
import de.luckymcdev.foundryengine.client.editor.builtin.cutscene.CutsceneTimelinePanel;
import de.luckymcdev.foundryengine.client.editor.builtin.explorer.FileExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.explorer.ResourceExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.scene.ScenePanel;
import de.luckymcdev.foundryengine.client.editor.builtin.tools.*;
import de.luckymcdev.foundryengine.client.editor.builtin.view.InfoPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.view.ThemeSelectorPanel;
import de.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent;
import de.luckymcdev.foundryengine.client.event.RegisterRenderingStuffEvent;
import de.luckymcdev.foundryengine.client.ext.ModPathBroadcaster;
import de.luckymcdev.foundryengine.client.icons.ScreenIconExporter;
import de.luckymcdev.foundryengine.client.render.entity.EngineEntityRenderers;
import de.luckymcdev.foundryengine.client.scene.ClientSceneSync;
import de.luckymcdev.foundryengine.client.scene.SelectionManager;
import de.luckymcdev.foundryengine.client.util.key.RegisterKeyBindingEvent;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.BundleHashPacket;
import de.luckymcdev.foundryengine.common.util.FolderHash;
import de.luckymcdev.foundryengine.config.ClientConfig;
import de.luckymcdev.foundryengine.config.Config;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(value = Common.MODID, dist = Dist.CLIENT)
public class FoundryEngineModClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final IEventBus BUS = NeoForge.EVENT_BUS;
    private boolean hasIconAutoExported = false;

    public FoundryEngineModClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::addClientReloadListener);
        modBus.addListener(this::onRegisterKeyMapping);
        modBus.addListener(this::onRegisterDebugEntry);
        modBus.addListener(this::onRegisterDebugRenderers);
        modBus.addListener(this::onRegistry);
        modBus.addListener(EngineEntityRenderers::onRegisterRenderers);

        BUS.addListener(this::onRegisterKeyBinding);
        BUS.addListener(this::onRegisterPanels);
        BUS.addListener(this::onClientTick);
        BUS.addListener(this::onRenderLevel);
        BUS.addListener(this::onRegisterCommands);
        BUS.addListener(this::onLoggingIn);

        Config.registerClient(modContainer);
    }

    private void onRegistry(RegistryEvent event) {
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.debug("FoundryEngineModClient setup called");

        ModPathBroadcaster.onClientSetup();

        Common.getBundleManager().loadClientScripts();

        event.enqueueWork(() -> {
            BUS.post(new RegisterRenderingStuffEvent(Client.getResourceManager()));
            BUS.post(new RegisterPanelEvent());
        });
    }

    private void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        try {
            String hashcode = FolderHash.hashFolder(Common.BUNDLES);
            ClientPacketDistributor.sendToServer(new BundleHashPacket(hashcode));
        } catch (Exception e) {
            LOGGER.error("Failed to hash bundles folder", e);
        }
    }

    private void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        BUS.post(new RegisterKeyBindingEvent(Client.getKeyBindingManager()));
        Client.getKeyBindingManager().getKeyBindings().forEach(keyBinding ->
                event.register(keyBinding.mapping())
        );
        event.registerCategory(Client.EDITOR_CATEGORY);
    }

    private void onRegisterKeyBinding(RegisterKeyBindingEvent event) {
        event.register(Client.EDITOR_KEY);
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
        event.register(ScenePanel.INSTANCE);
        event.register(CataloguePanel.INSTANCE);
        event.register(ThemeSelectorPanel.INSTANCE);
        event.register(EffectPanel.INSTANCE);
        event.register(BlueprintsPanel.INSTANCE);
        event.register(AreaPanel.INSTANCE);
        event.register(CutscenePanel.INSTANCE);
        event.register(CutsceneTimelinePanel.INSTANCE);
    }

    private void addClientReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(Common.id("imgui_handler"), Client.getImGuiManager());
    }

    private void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
        ClientCutsceneManager.renderTick();
        ClientScreenEffectManager.renderTick();
        CutsceneRenderer.render();
        AreaRenderer.render();
        var selectedNode = SelectionManager.getSelected();
        if (ScenePanel.INSTANCE.showGizmos && selectedNode != null) {
            selectedNode.drawGizmos();
        }
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Client.getEditorManager().handleTick();

        ClientSceneSync.clientTick();

        ClientCutsceneManager.clientTick();
        CutsceneEditor.clientTick();

        if (ClientConfig.AUTO_EXPORT.get() && !hasIconAutoExported && Minecraft.getInstance().level != null) {
            if (Minecraft.getInstance().screen != null) return;

            hasIconAutoExported = true;

            LOGGER.info("Auto-export: Initializing icon generation...");
            double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
            ScreenIconExporter screen = new ScreenIconExporter(
                    Minecraft.getInstance().level.registryAccess(),
                    guiScale,
                    null,
                    false
            );

            if (screen.hasWork()) {
                Minecraft.getInstance().setScreen(screen);
            } else {
                LOGGER.info("Auto-export: All icons are up to date.");
            }
        }
    }
}
