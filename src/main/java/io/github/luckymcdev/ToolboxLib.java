package io.github.luckymcdev;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.client.RegisterRenderingStuffEvent;
import io.github.luckymcdev.client.imgui.ImGuiHandler;
import io.github.luckymcdev.client.post.RegisterPostPipelineEvent;
import io.github.luckymcdev.client.render.TestRender;
import io.github.luckymcdev.client.util.KeyBinding;
import io.github.luckymcdev.client.util.RegisterKeyBindingEvent;
import io.github.luckymcdev.common.Commons;
import io.github.luckymcdev.common.Instances;
import io.github.luckymcdev.common.opencl.OpenClExample;
import io.github.luckymcdev.common.opencl.task.ClWorker;
import io.github.luckymcdev.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.GameRenderer;
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
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import javax.script.CompiledScript;

@Mod(Commons.MODID)
public class ToolboxLib {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ToolboxLib(IEventBus modEventBus, ModContainer modContainer) {

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
        }

        @SubscribeEvent
        public static void onRegisterKeyBinding(RegisterKeyBindingEvent event) {
            event.register(new KeyBinding(
                    new KeyMapping("key.toolboxlib.testkey", GLFW.GLFW_KEY_O, KeyMapping.Category.MISC),
                    () -> Instances.getMinecraft().player.displayClientMessage(Component.literal("Hello"), false)
            ));
        }

        @SubscribeEvent
        public static void onRegisterPostPipelines(RegisterPostPipelineEvent event) {
            TestRender.registerPipelines();
        }

        @SubscribeEvent
        public static void onRegisterKeyMapping(RegisterKeyMappingsEvent event) {
            NeoForge.EVENT_BUS.post(new RegisterKeyBindingEvent(Instances.getKeyBindingManager()));
        }

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
            if (!shadersInitialized) {
                shadersInitialized = true;
                NeoForge.EVENT_BUS.post(new RegisterPostPipelineEvent());
                NeoForge.EVENT_BUS.post(new RegisterRenderingStuffEvent(Instances.getTbRenderer(), Instances.getResourceManager()));
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
