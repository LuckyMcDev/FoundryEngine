package io.github.luckymcdev;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.common.Commons;
import io.github.luckymcdev.common.Instances;
import io.github.luckymcdev.common.opencl.OpenClExample;
import io.github.luckymcdev.common.opencl.task.ClWorker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(Commons.MODID)
public class ToolboxLib {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ToolboxLib(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        /*
        OpenCLExample.test();
        OpenCLExample.testHills();
        OpenCLExample.testPlains();
        OpenCLExample.testMountains();
        OpenCLExample.visualizeColorized();
         */

        ClWorker.submit(() -> {
            OpenClExample.visualize(15630, 8640, 300.0f, 32, false);
            //OpenCLExample.comparePerformance(7680, 4320, 300.0f, 6, 0.5f, 10);
            return "1";
        });


    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
    }

    @EventBusSubscriber(modid = Commons.MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Instances.getBuiltInEditor().handleTick();
        }
    }
}
