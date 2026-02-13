package io.github.luckymcdev.client;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.client.opengl.framebuffer.FrameBufferManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@EventBusSubscriber
public class TbRenderer implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final FrameBufferManager frameBufferManager;


    public TbRenderer() {
        this.frameBufferManager = new FrameBufferManager();
    }


    public FrameBufferManager getFrameBufferManager() {
        return frameBufferManager;
    }

    @SubscribeEvent
    private static void updateClientMatrices(FrameGraphSetupEvent event) {
        ClientMatrices.updateMain(event.getModelViewMatrix(), event.getProjectionMatrix());
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        LOGGER.info("This is displayed on a resource Reload");
    }
}
