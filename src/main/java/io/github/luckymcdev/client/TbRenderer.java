package io.github.luckymcdev.client;

import io.github.luckymcdev.client.gl.framebuffer.FrameBufferManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;

@EventBusSubscriber
public class TbRenderer implements ResourceManagerReloadListener {
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
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }
}
