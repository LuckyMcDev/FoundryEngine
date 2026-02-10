package io.github.luckymcdev.client.gl.framebuffer;

import io.github.luckymcdev.common.registry.GenericRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

public class FrameBufferManager {
    public static final GenericRegistry<ResourceLocation, FrameBuffer> FRAMEBUFFERS = new GenericRegistry<>();


    public static void register(FrameBuffer frameBuffer) {
        FRAMEBUFFERS.register(frameBuffer.id(), frameBuffer);
    }

    @ApiStatus.Internal
    public static void resize(int width, int height) {
        FRAMEBUFFERS.getValues().forEach(fbo -> {
            fbo.resize(width, height);
        });
    }

}
