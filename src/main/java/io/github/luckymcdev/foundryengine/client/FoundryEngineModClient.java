package io.github.luckymcdev.foundryengine.client;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.editor.builtin.BrowserPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.ConsolePanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.FileExplorerPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.TestPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.editor.MainEditor;
import io.github.luckymcdev.foundryengine.client.editor.builtin.imnodes.GroovyEditorPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.post.PostProcessPanel;
import io.github.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent;
import io.github.luckymcdev.foundryengine.client.event.RegisterRenderingStuffEvent;
import io.github.luckymcdev.foundryengine.client.gui.debug.BundleDebugEntry;
import io.github.luckymcdev.foundryengine.client.gui.debug.PostProcessDebugEntry;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.IncludeGLSLPreProcessor;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.RegisterGLSLPreProcessorEvent;
import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.*;
import io.github.luckymcdev.foundryengine.client.util.RegisterKeyBindingEvent;
import io.github.luckymcdev.foundryengine.common.Common;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@EventBusSubscriber(modid = Common.MODID, value = Dist.CLIENT)
public class FoundryEngineModClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final IEventBus BUS = NeoForge.EVENT_BUS;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.debug("FoundryEngineModClient setup called");

        event.enqueueWork(() -> {
            BUS.post(new RegisterRenderingStuffEvent(Client.getResourceManager()));
            BUS.post(new RegisterGLSLPreProcessorEvent());
            BUS.post(new RegisterPanelEvent());
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        BUS.post(new RegisterKeyBindingEvent(Client.getKeyBindingManager()));
        Client.getKeyBindingManager().getKeyBindings().forEach(keyBinding ->
                event.register(keyBinding.mapping())
        );
    }

    @SubscribeEvent
    public static void onRegisterKeyBindingEvent(RegisterKeyBindingEvent event) {
        event.register(Client.EDITOR_KEY);
    }

    @SubscribeEvent
    public static void onRegisterGLSLPreProcessors(RegisterGLSLPreProcessorEvent event) {
        event.register(new IncludeGLSLPreProcessor());
    }

    @SubscribeEvent
    public static void onRegisterDebugEntry(RegisterDebugEntriesEvent event) {
        event.register(Common.id("bundles_info"), new BundleDebugEntry(Common.getBundleManager()));
        event.register(Common.id("post_info"), new PostProcessDebugEntry(Client.getPostProcessManager()));
    }

    @SubscribeEvent
    public static void onRegisterPanels(RegisterPanelEvent event) {
        event.register(PostProcessPanel.INSTANCE);
        event.register(TestPanel.INSTANCE);
        if (ModList.get().isLoaded("mcef")) {
            event.register(BrowserPanel.INSTANCE);
        }

        event.register(FileExplorerPanel.INSTANCE);

        event.register(ConsolePanel.INSTANCE);

        event.register(GroovyEditorPanel.INSTANCE);

        event.register(MainEditor.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterPostPipelines(RegisterPostPipelineEvent event) {
        event.register(new GrayscalePipeline());
        event.register(new DepthVisualizePipeline());
        event.register(new AsciiPostProcessPipeline());
        event.register(new UpsideDownPipeline());
        event.register(new InvertedColorsPipeline());
        event.register(new CRTScanlinePipeline());
    }

    @SubscribeEvent
    public static void addClientReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(Common.id("imgui_handler"), Client.getImGuiManager());
        event.addListener(Common.id("shader_manager"), Client.getShaderManager());
        event.addListener(Common.id("post_pipeline_init"),
                (ResourceManagerReloadListener) resourceManager ->
                        BUS.post(new RegisterPostPipelineEvent(Client.getPostProcessManager()))
        );
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Client.getEditorManager().handleTick();
    }

    @SubscribeEvent
    public static void updateClientMatrices(FrameGraphSetupEvent event) {
        Client.updateMain(event.getModelViewMatrix(), event.getProjectionMatrix());
    }
}
