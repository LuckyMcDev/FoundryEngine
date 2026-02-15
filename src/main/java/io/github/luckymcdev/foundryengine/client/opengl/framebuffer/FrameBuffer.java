package io.github.luckymcdev.foundryengine.client.opengl.framebuffer;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlObject;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL43C;

/**
 * A wrapper around a OpenGl FrameBuffer
 */
public class FrameBuffer extends OpenGlObject {

    private final Identifier id;
    private final boolean hasDepth;
    private final boolean hasStencil;
    private final int clearMask;
    private int width;
    private int height;
    private int colorTexture;
    private int depthTexture;
    private int filterMode = GL43C.GL_LINEAR;
    private final float[] clearColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f};

    /**
     * Instantiates a new Frame buffer.
     *
     * @param id         the id
     * @param width      the width
     * @param height     the height
     * @param useDepth   uses depth
     * @param useStencil uses stencil
     */
    public FrameBuffer(Identifier id, int width, int height, boolean useDepth, boolean useStencil) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.hasDepth = useDepth;
        this.hasStencil = useStencil && useDepth;

        int mask = GL43C.GL_COLOR_BUFFER_BIT;
        if (this.hasDepth) mask |= GL43C.GL_DEPTH_BUFFER_BIT;
        if (this.hasStencil) mask |= GL43C.GL_STENCIL_BUFFER_BIT;
        this.clearMask = mask;

        this.pointer = GlDispatch.glGenFramebuffers();
        bind(false);
        createAttachments();
        validateFramebufferComplete();
        unbind();
        setDebugLabel(this.id.toString());
    }

    /**
     * From target frame buffer.
     *
     * @param id     the id
     * @param target the target
     * @return the frame buffer
     */
    public static @NotNull FrameBuffer fromTarget(Identifier id, @NotNull RenderTarget target) {
        RenderSystem.assertOnRenderThread();
        if (target.getColorTexture() == null) {
            throw new IllegalArgumentException("RenderTarget has no color texture");
        }

        GlTexture colorTexture = Instances.getGlColTexture();
        GlTexture depthTexture = target.getDepthTexture() != null ? Instances.getGlDepthTexture() : null;
        GlDevice device = Instances.getGlDevice();

        FrameBuffer buffer = new FrameBuffer(id, target.width, target.height, target.getDepthTexture() != null, target.useStencil);
        int sourceFbo = colorTexture.getFbo(device.directStateAccess(), depthTexture);

        GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, sourceFbo);
        GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, buffer.pointer);

        GlDispatch.glBlitFramebuffer(0, 0, target.width, target.height, 0, 0, buffer.width, buffer.height, buffer.clearMask, GL43C.GL_NEAREST);

        GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, 0);
        GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, 0);
        return buffer;
    }

    /**
     * Bind.
     *
     * @param setViewport weather to set the viewport
     */
    public void bind(boolean setViewport) {
        GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, this.pointer);
        if (setViewport) GlDispatch.glViewport(0, 0, this.width, this.height);
    }

    /**
     * Bind.
     */
    public void bind() {
        bind(true);
    }

    /**
     * Unbind.
     */
    public void unbind() {
        GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, 0);
    }

    /**
     * Bind color texture.
     */
    public void bindColorTexture() {
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, colorTexture);
    }

    /**
     * Bind depth texture.
     */
    public void bindDepthTexture() {
        if (hasDepth) GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, depthTexture);
    }

    /**
     * Clear.
     */
    public void clear() {
        bind(false);
        GlDispatch.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        GlDispatch.glClear(clearMask);
    }

    /**
     * Resize.
     *
     * @param width  the width
     * @param height the height
     */
    public void resize(int width, int height) {
        if (this.width == width && this.height == height) return;
        this.width = width;
        this.height = height;
        bind(false);
        deleteAttachments();
        createAttachments();
        validateFramebufferComplete();
        unbind();
    }

    private void createAttachments() {
        colorTexture = GlDispatch.glGenTextures();
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, colorTexture);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MIN_FILTER, filterMode);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MAG_FILTER, filterMode);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_WRAP_S, GL43C.GL_CLAMP_TO_EDGE);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_WRAP_T, GL43C.GL_CLAMP_TO_EDGE);
        GlDispatch.glTexImage2D(GL43C.GL_TEXTURE_2D, 0, GL43C.GL_RGBA8, width, height, 0, GL43C.GL_RGBA, GL43C.GL_UNSIGNED_BYTE, null);
        GlDispatch.glFramebufferTexture2D(GL43C.GL_FRAMEBUFFER, GL43C.GL_COLOR_ATTACHMENT0, GL43C.GL_TEXTURE_2D, colorTexture, 0);

        if (hasDepth) {
            depthTexture = GlDispatch.glGenTextures();
            GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, depthTexture);
            int internalFormat = hasStencil ? GL43C.GL_DEPTH24_STENCIL8 : GL43C.GL_DEPTH_COMPONENT24;
            int format = hasStencil ? GL43C.GL_DEPTH_STENCIL : GL43C.GL_DEPTH_COMPONENT;
            int type = hasStencil ? GL43C.GL_UNSIGNED_INT_24_8 : GL43C.GL_FLOAT;

            GlDispatch.glTexImage2D(GL43C.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, null);
            GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MIN_FILTER, GL43C.GL_NEAREST);
            GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MAG_FILTER, GL43C.GL_NEAREST);
            GlDispatch.glFramebufferTexture2D(GL43C.GL_FRAMEBUFFER, GL43C.GL_DEPTH_ATTACHMENT, GL43C.GL_TEXTURE_2D, depthTexture, 0);
            if (hasStencil) GlDispatch.glFramebufferTexture2D(GL43C.GL_FRAMEBUFFER, GL43C.GL_STENCIL_ATTACHMENT, GL43C.GL_TEXTURE_2D, depthTexture, 0);
        }
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, 0);
    }

    private void deleteAttachments() {
        if (colorTexture != 0) GlDispatch.glDeleteTextures(colorTexture);
        if (depthTexture != 0) GlDispatch.glDeleteTextures(depthTexture);
        colorTexture = 0;
        depthTexture = 0;
    }

    private void validateFramebufferComplete() {
        int status = GlDispatch.glCheckFramebufferStatus(GL43C.GL_FRAMEBUFFER);
        if (status != GL43C.GL_FRAMEBUFFER_COMPLETE) {
            String error = switch (status) {
                case GL43C.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "INCOMPLETE_ATTACHMENT";
                case GL43C.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "INCOMPLETE_MISSING_ATTACHMENT";
                case GL43C.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "INCOMPLETE_DRAW_BUFFER";
                case GL43C.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "INCOMPLETE_READ_BUFFER";
                case GL43C.GL_FRAMEBUFFER_UNSUPPORTED -> "UNSUPPORTED";
                case GL43C.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "INCOMPLETE_MULTISAMPLE";
                default -> "UNKNOWN (" + status + ")";
            };
            throw new RuntimeException("Framebuffer " + id + " is not complete: " + error);
        }
    }

    private void setDebugLabel(String label) {
        if (label != null && this.pointer != 0) GlDispatch.safeGlObjectLabel(GL43C.GL_FRAMEBUFFER, this.pointer, label);
    }

    @Override
    public void free() {
        deleteAttachments();
        if (this.pointer != 0) GlDispatch.glDeleteFramebuffers(this.pointer);
        this.pointer = 0;
    }

    /**
     * Gets color texture.
     *
     * @return the color texture
     */
    public int getColorTexture() { return colorTexture; }

    /**
     * Gets depth texture.
     *
     * @return the depth texture
     */
    public int getDepthTexture() { return depthTexture; }

    /**
     * Width int.
     *
     * @return the int
     */
    public int width() { return width; }

    /**
     * Height int.
     *
     * @return the int
     */
    public int height() { return height; }

    /**
     * Has depth attachment boolean.
     *
     * @return the boolean
     */
    public boolean hasDepthAttachment() { return hasDepth; }

    /**
     * Has stencil boolean.
     *
     * @return the boolean
     */
    public boolean hasStencil() {
        return hasStencil;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public Identifier getId() {
        return id;
    }
}