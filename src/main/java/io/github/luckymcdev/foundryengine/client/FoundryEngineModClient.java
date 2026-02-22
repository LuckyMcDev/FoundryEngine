package io.github.luckymcdev.foundryengine.client;

import io.github.luckymcdev.foundryengine.client.editor.builtin.NodeEditorPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.PostProcessPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.TestPanel;
import io.github.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent;
import io.github.luckymcdev.foundryengine.client.event.RegisterRenderingStuffEvent;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.IncludeGLSLPreProcessor;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.RegisterGLSLPreProcessorEvent;
import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.AsciiPostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.DepthVisualizePipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.GrayscalePipeline;
import io.github.luckymcdev.foundryengine.client.util.RegisterKeyBindingEvent;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Commons.MODID, value = Dist.CLIENT)
public class FoundryEngineModClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Instances.post(new RegisterRenderingStuffEvent(Instances.getResourceManager()));
            Instances.post(new RegisterGLSLPreProcessorEvent());
            Instances.post(new RegisterPanelEvent());
            Instances.post(new RegisterPostPipelineEvent(Instances.getPostProcessManager()));
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
        Instances.post(new RegisterKeyBindingEvent(Instances.getKeyBindingManager()));
        Instances.getKeyBindingManager().getKeyBindings().forEach(keyBinding ->
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
    }

    @SubscribeEvent
    public static void onRegisterPostPipelines(RegisterPostPipelineEvent event) {
        event.register(new GrayscalePipeline());
        event.register(new DepthVisualizePipeline());
        event.register(new AsciiPostProcessPipeline());
    }

    @SubscribeEvent
    public static void addClientReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(Commons.id("imgui_handler"), Instances.getImGuiManager());
        event.addListener(Commons.id("shader_manager"), Instances.getShaderManager());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Instances.getEditorManager().handleTick();
    }

    @SubscribeEvent
    public static void updateClientMatrices(FrameGraphSetupEvent event) {
        ClientMatrices.updateMain(event.getModelViewMatrix(), event.getProjectionMatrix());
    }
}
