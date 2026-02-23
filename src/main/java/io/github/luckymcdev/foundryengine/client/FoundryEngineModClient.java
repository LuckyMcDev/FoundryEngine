package io.github.luckymcdev.foundryengine.client;

import io.github.luckymcdev.foundryengine.client.editor.builtin.TestPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.editor.MainEditorPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.node.NodeEditorPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.post.PostProcessPanel;
import io.github.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent;
import io.github.luckymcdev.foundryengine.client.event.RegisterRenderingStuffEvent;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.IncludeGLSLPreProcessor;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.RegisterGLSLPreProcessorEvent;
import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.*;
import io.github.luckymcdev.foundryengine.client.util.RegisterKeyBindingEvent;
import io.github.luckymcdev.foundryengine.common.Common;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Common.MODID, value = Dist.CLIENT)
public class FoundryEngineModClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Common.post(new RegisterRenderingStuffEvent(Client.getResourceManager()));
            Common.post(new RegisterGLSLPreProcessorEvent());
            Common.post(new RegisterPanelEvent());
            Common.post(new RegisterPostPipelineEvent(Client.getPostProcessManager()));
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        Common.post(new RegisterKeyBindingEvent(Client.getKeyBindingManager()));
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
    public static void onRegisterPanels(RegisterPanelEvent event) {
        event.register(PostProcessPanel.INSTANCE);
        event.register(TestPanel.INSTANCE);
        event.register(NodeEditorPanel.INSTANCE);

        event.register(MainEditorPanel.INSTANCE);
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
