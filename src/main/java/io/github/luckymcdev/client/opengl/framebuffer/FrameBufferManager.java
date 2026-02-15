package io.github.luckymcdev.client.opengl.framebuffer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.luckymcdev.client.opengl.GlDispatch;
import io.github.luckymcdev.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL43C;

/**
 * The type Frame buffer manager.
 */
public class FrameBufferManager {
    private static final GenericRegistry<Identifier, FrameBuffer> FRAMEBUFFERS = new GenericRegistry<>();


    /**
     * Register.
     *
     * @param frameBuffer the frame buffer
     */
    public void register(FrameBuffer frameBuffer) {
        FRAMEBUFFERS.register(frameBuffer.getId(), frameBuffer);
    }

    /**
     * Blit.
     *
     * @param src    the src
     * @param dstFbo the dst fbo
     * @param dst    the dst
     */
    public void blit(FrameBuffer src, int dstFbo, RenderTarget dst) {
        // Save the current framebuffer bindings
        int prevRead = GlDispatch.glGetInteger(GL43C.GL_READ_FRAMEBUFFER_BINDING);
        int prevDraw = GlDispatch.glGetInteger(GL43C.GL_DRAW_FRAMEBUFFER_BINDING);

        try {
            // Set up for blitting
            GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, src.pointer());
            GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, dstFbo);

            int mask = GL43C.GL_COLOR_BUFFER_BIT;
            if (src.hasDepthAttachment() && dst.getDepthTexture() != null) {
                mask |= GL43C.GL_DEPTH_BUFFER_BIT;
            }
            if (src.hasStencil() && dst.useStencil) {
                mask |= GL43C.GL_STENCIL_BUFFER_BIT;
            }

            GlDispatch.glBlitFramebuffer(
                    0, 0, src.width(), src.height(),
                    0, 0, dst.width, dst.height,
                    mask,
                    GL43C.GL_NEAREST
            );
        } finally {
            // Always restore the previous framebuffer state, even if blitting failed
            GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, prevRead);
            GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, prevDraw);
            // Ensure both read and draw are pointing to the same target for normal rendering
            GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, prevDraw);
        }
    }
}