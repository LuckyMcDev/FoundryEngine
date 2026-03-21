package de.luckymcdev.foundryengine.client.opengl.framebuffer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.opengl.GlDispatch;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL43C;

/**
 * Manages FrameBuffers.
 * Currently, it only does Blit.
 */
public class FrameBufferManager {
    private static final GenericRegistry<Identifier, FrameBuffer> FRAMEBUFFERS = new GenericRegistry<>();

    public FrameBufferManager() {
    }

    /**
     * Register.
     *
     * @param frameBuffer the frame buffer
     */
    public void register(FrameBuffer frameBuffer) {
        FRAMEBUFFERS.register(frameBuffer.getId(), frameBuffer);
    }

    /**
     * Blit FROM a {@link FrameBuffer} TO a {@link RenderTarget}
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

    /**
     * Blit FROM {@link RenderTarget} TO {@link FrameBuffer}
     *
     * @param src source {@link RenderTarget}
     * @param dst destination {@link FrameBuffer}
     */
    public void blit(RenderTarget src, FrameBuffer dst) {
        var colorTexture = Client.getGlColTexture(src);
        var device = Client.getGlDevice();
        int srcFbo = colorTexture.getFbo(device.directStateAccess(), null);

        // Save current bindings
        int prevRead = GlDispatch.glGetInteger(GL43C.GL_READ_FRAMEBUFFER_BINDING);
        int prevDraw = GlDispatch.glGetInteger(GL43C.GL_DRAW_FRAMEBUFFER_BINDING);

        try {
            // Source is the RenderTarget FBO
            GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, srcFbo);
            // Destination is our FrameBuffer object
            GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, dst.pointer());

            // We usually only need color for the backup snapshot
            int mask = GL43C.GL_COLOR_BUFFER_BIT;

            GlDispatch.glBlitFramebuffer(
                    0, 0, src.width, src.height,
                    0, 0, dst.width(), dst.height(),
                    mask,
                    GL43C.GL_NEAREST
            );
        } finally {
            GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, prevRead);
            GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, prevDraw);
            GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, prevDraw);
        }
    }

    /**
     * Blit FROM {@link FrameBuffer} TO {@link RenderTarget}
     * @param src source {@link FrameBuffer}
     * @param dst destination {@link RenderTarget}
     */
    public void blit(FrameBuffer src, RenderTarget dst) {
        var colorTexture = Client.getGlColTexture(dst);
        var device = Client.getGlDevice();
        int dstFbo = colorTexture.getFbo(device.directStateAccess(), null);

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