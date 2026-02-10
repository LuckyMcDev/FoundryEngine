package io.github.luckymcdev.client;

import io.github.luckymcdev.client.gl.framebuffer.FrameBufferManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class TbRenderer implements ResourceManagerReloadListener {
    private final FrameBufferManager frameBufferManager;


    public TbRenderer() {
        this.frameBufferManager = new FrameBufferManager();
    }


    public FrameBufferManager getFrameBufferManager() {
        return frameBufferManager;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }
}
