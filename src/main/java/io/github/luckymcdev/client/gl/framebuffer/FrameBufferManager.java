package io.github.luckymcdev.client.gl.framebuffer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.common.registry.GenericRegistry;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL43C;

public class FrameBufferManager {
    public static final GenericRegistry<ResourceLocation, FrameBuffer> FRAMEBUFFERS = new GenericRegistry<>();


    public void register(FrameBuffer frameBuffer) {
        FRAMEBUFFERS.register(frameBuffer.id(), frameBuffer);
    }

    public void blit(FrameBuffer src, int dstFbo, RenderTarget dst) {
        int prevRead = GlDispatch.glGetInteger(GL43C.GL_READ_FRAMEBUFFER_BINDING);
        int prevDraw = GlDispatch.glGetInteger(GL43C.GL_DRAW_FRAMEBUFFER_BINDING);

        GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, src.pointer());
        GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, dstFbo);

        int mask = GL43C.GL_COLOR_BUFFER_BIT;
        if (src.hasDepthAttachment() && dst.getDepthTexture() != null) {
            mask |= GL43C.GL_DEPTH_BUFFER_BIT;
        }
        if (src.hasStencilAttachment() && dst.useStencil) {
            mask |= GL43C.GL_STENCIL_BUFFER_BIT;
        }

        GlDispatch.glBlitFramebuffer(
                0, 0, src.width(), src.height(),
                0, 0, dst.width, dst.height,
                mask,
                GL43C.GL_NEAREST
        );

        GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, prevRead);
        GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, prevDraw);
    }

}
