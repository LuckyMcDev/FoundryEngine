package de.luckymcdev.foundryengine.client;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.RegistryEvent;
import de.luckymcdev.foundryengine.client.debug.renderer.SimpleDebugScreenRenderer;
import de.luckymcdev.foundryengine.client.debug.screen.BundleDebugEntry;
import de.luckymcdev.foundryengine.client.debug.screen.GameStagesDebugEntry;
import de.luckymcdev.foundryengine.client.editor.builtin.MainEditor;
import de.luckymcdev.foundryengine.client.editor.builtin.TestPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.explorer.FileExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.explorer.ResourceExplorerPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.scene.ScenePanel;
import de.luckymcdev.foundryengine.client.editor.builtin.tools.CataloguePanel;
import de.luckymcdev.foundryengine.client.editor.builtin.tools.ConsolePanel;
import de.luckymcdev.foundryengine.client.editor.builtin.tools.MinecraftToolsPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.tools.StopwatchPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.view.InfoPanel;
import de.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent;
import de.luckymcdev.foundryengine.client.event.RegisterRenderingStuffEvent;
import de.luckymcdev.foundryengine.client.ext.ModPathBroadcaster;
import de.luckymcdev.foundryengine.client.icons.ScreenIconExporter;
import de.luckymcdev.foundryengine.client.particle.EngineParticles;
import de.luckymcdev.foundryengine.client.util.key.RegisterKeyBindingEvent;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.config.ClientConfig;
import de.luckymcdev.foundryengine.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Dedicated Client Mod Entrypoint for FoundryEngine.
 */
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
        modBus.addListener(this::onRegisterClientPayloadHandlers);
        modBus.addListener(this::onRegistry);

        BUS.addListener(this::onRegisterKeyBinding);
        BUS.addListener(this::onRegisterPanels);
        BUS.addListener(this::onClientTick);

        Config.registerClient(modContainer);
    }

    private void onRegistry(RegistryEvent event) {
        EngineParticles.register(event);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.debug("FoundryEngineModClient setup called");

        ModPathBroadcaster.onClientSetup();

        event.enqueueWork(() -> {
            BUS.post(new RegisterRenderingStuffEvent(Client.getResourceManager()));
            BUS.post(new RegisterPanelEvent());
        });
    }

    private void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        BUS.post(new RegisterKeyBindingEvent(Client.getKeyBindingManager()));
        Client.getKeyBindingManager().getKeyBindings().forEach(keyBinding ->
                event.register(keyBinding.mapping())
        );
    }

    private void onRegisterKeyBinding(RegisterKeyBindingEvent event) {
        event.register(Client.EDITOR_KEY);
    }

    private void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
    }

    private void onRegisterDebugEntry(RegisterDebugEntriesEvent event) {
        event.register(Common.id("bundles_info"), new BundleDebugEntry(Common.getBundleManager()));
        event.register(Common.id("gamestages_info"), new GameStagesDebugEntry());
    }

    private void onRegisterDebugRenderers(RegisterDebugRenderersEvent event) {
        event.register(minecraft -> new SimpleDebugScreenRenderer(
                minecraft,
                (mc, camPos, debugValueAccess, frustum, partialTick) -> {
                    camPos.add(0, 2, 0);
                    Gizmos.billboardText(
                            "TEST",
                            new Vec3(camPos.x(), camPos.y(), camPos.z()),
                            TextGizmo.Style.forColorAndCentered(0xFF00FF).withScale(2F));
                }
        ));
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
    }

    private void addClientReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(Common.id("imgui_handler"), Client.getImGuiManager());
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Client.getEditorManager().handleTick();
        EngineParticles.tick();

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