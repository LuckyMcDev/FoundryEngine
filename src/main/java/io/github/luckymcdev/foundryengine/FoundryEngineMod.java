package io.github.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.ClientMatrices;
import io.github.luckymcdev.foundryengine.client.RegisterRenderingStuffEvent;
import io.github.luckymcdev.foundryengine.client.editor.builtin.NodeEditorPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.PostProcessPanel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.TestPanel;
import io.github.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.IncludeGLSLPreProcessor;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.RegisterGLSLPreProcessorEvent;
import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.AsciiPostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.DepthVisualizePipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.GrayscalePipeline;
import io.github.luckymcdev.foundryengine.client.util.RegisterKeyBindingEvent;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import io.github.luckymcdev.foundryengine.common.opencl.ClDispatch;
import io.github.luckymcdev.foundryengine.common.opencl.OpenClExample;
import io.github.luckymcdev.foundryengine.common.thread.RegisterEngineThreadEvent;
import io.github.luckymcdev.foundryengine.config.Config;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Main Mod Entrypoint for FoundryEngine.
 */
@Mod(Commons.MODID)
public class FoundryEngineMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Initializes the mod and registers events.
     *
     * @param modEventBus  the mod event bus
     * @param modContainer the mod container
     */
    public FoundryEngineMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        Instances.post(new RegisterEngineThreadEvent());

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Instances.getThreadManager().execute(ClDispatch.CL_THREAD, () -> {
            OpenClExample.visualize(15630, 8640, 300.0f, 32, false);
        });
    }

    @SubscribeEvent
    public void onRegisterEngineThread(RegisterEngineThreadEvent event) {
        event.register(ClDispatch.CL_THREAD);
    }

    /**
     * Container for client-side event handlers.
     */
    @EventBusSubscriber(modid = Commons.MODID, value = Dist.CLIENT)
    public static class ClientModEvents {

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
            event.register(Commons.EDITOR_KEY);
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
}
