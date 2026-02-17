package io.github.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.ClientMatrices;
import io.github.luckymcdev.foundryengine.client.RegisterRenderingStuffEvent;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.IncludeGLSLPreProcessor;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.preprocessing.RegisterGLSLPreProcessorEvent;
import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent;
import io.github.luckymcdev.foundryengine.client.TestRender;
import io.github.luckymcdev.foundryengine.client.util.KeyBinding;
import io.github.luckymcdev.foundryengine.client.util.RegisterKeyBindingEvent;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import io.github.luckymcdev.foundryengine.common.opencl.OpenClExample;
import io.github.luckymcdev.foundryengine.common.opencl.task.ClWorker;
import io.github.luckymcdev.foundryengine.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(Commons.MODID)
public class FoundryEngineMod {
    private static final Logger LOGGER = LogUtils.getLogger();

    public FoundryEngineMod(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ClWorker.submit(() -> {
            OpenClExample.visualize(15630, 8640, 300.0f, 32, false);
            return "1";
        });
    }

    @SubscribeEvent
    public void registerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Commons.id("tbrenderer"),Instances.getTbRenderer());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
    }

    @EventBusSubscriber(modid = Commons.MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        private static boolean shadersInitialized = false;

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            NeoForge.EVENT_BUS.post(new RegisterGLSLPreProcessorEvent());
        }

        @SubscribeEvent
        public static void onRegisterGLSLPreProcessors(RegisterGLSLPreProcessorEvent event) {
            event.register(new IncludeGLSLPreProcessor());
        }

        @SubscribeEvent
        public static void onRegisterKeyBinding(RegisterKeyBindingEvent event) {
            event.register(new KeyBinding(
                    new KeyMapping("key.foundryengine.testkey", GLFW.GLFW_KEY_O, KeyMapping.Category.MISC),
                    () -> Instances.getMinecraft().player.displayClientMessage(Component.literal("Hello"), false)
            ));
        }

        @SubscribeEvent
        public static void onRegisterPostPipelines(RegisterPostPipelineEvent event) {
            TestRender.registerPipelines(event);
        }

        @SubscribeEvent
        private static void updateClientMatrices(FrameGraphSetupEvent event) {
            ClientMatrices.updateMain(event.getModelViewMatrix(), event.getProjectionMatrix());
        }

        @SubscribeEvent
        public static void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
            NeoForge.EVENT_BUS.post(new RegisterKeyBindingEvent(Instances.getKeyBindingManager()));
        }

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
            if (!shadersInitialized) {
                shadersInitialized = true;
                NeoForge.EVENT_BUS.post(new RegisterPostPipelineEvent(Instances.getPostProcessManager()));
                NeoForge.EVENT_BUS.post(new RegisterRenderingStuffEvent(Instances.getResourceManager()));
            }
        }

        @SubscribeEvent
        public static void addClientReloadListener(AddClientReloadListenersEvent event) {
            event.addListener(Commons.id("imgui_handler"), Instances.getImGuiHandler());
            event.addListener(Commons.id("shader_manager"), Instances.getShaderManager());
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Instances.getBuiltInEditor().handleTick();
        }
    }
}
