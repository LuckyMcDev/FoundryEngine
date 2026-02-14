package io.github.luckymcdev.client.opengl.framebuffer;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.client.opengl.GlDispatch;
import io.github.luckymcdev.client.opengl.OpenGlObject;
import io.github.luckymcdev.common.Commons;
import io.github.luckymcdev.common.Instances;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL43C;

import java.util.Map;

import static org.lwjgl.opengl.GL43C.GL_SHADER;

public class FrameBuffer extends OpenGlObject {

    private final ResourceLocation id;
    private final boolean ownsFbo;
    private final boolean ownsAttachments;
    private final boolean hasDepth;
    private final boolean hasStencil;
    private final int clearMask;
    private int width;
    private int height;
    private int colorTexture;
    private int depthRenderBuffer;
    private int filterMode = GL43C.GL_LINEAR;
    private final float[] clearColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f};

    public FrameBuffer(ResourceLocation id, int width, int height) {
        this(id, width, height, true, false);
    }

    public FrameBuffer(ResourceLocation id, int width, int height, boolean useDepth) {
        this(id, width, height, useDepth, false);
    }

    public FrameBuffer(ResourceLocation id, int width, int height, boolean useDepth, boolean useStencil) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.ownsFbo = true;
        this.ownsAttachments = true;
        this.hasDepth = useDepth;
        this.hasStencil = useStencil && useDepth; // Stencil requires depth

        // Calculate clear mask based on attachments
        int mask = GL43C.GL_COLOR_BUFFER_BIT;
        if (this.hasDepth) {
            mask |= GL43C.GL_DEPTH_BUFFER_BIT;
        }
        if (this.hasStencil) {
            mask |= GL43C.GL_STENCIL_BUFFER_BIT;
        }
        this.clearMask = mask;

        this.pointer = GlDispatch.glGenFramebuffers();
        bind(false);
        createAttachments();
        unbind();
        setDebugLabel(this.id.toString());
    }

    public static @NotNull FrameBuffer fromTarget(@NotNull RenderTarget target) {
        RenderSystem.assertOnRenderThread();
        if (target.getColorTexture() == null) {
            throw new IllegalArgumentException("RenderTarget has no color texture");
        }

        GlTexture colorTexture = Instances.getGlColTexture();
        GlTexture depthTexture = null;
        if (target.getDepthTexture() != null) {
            depthTexture = Instances.getGlDepthTexture();
        }
        GlDevice device = Instances.getGlDevice();

        FrameBuffer buffer = new FrameBuffer(Commons.id(target.toString()), target.width, target.height, true);
        int sourceFbo = colorTexture.getFbo(device.directStateAccess(), depthTexture);

        GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, sourceFbo);
        GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, buffer.pointer);

        int mask = GL43C.GL_COLOR_BUFFER_BIT;
        if (target.getDepthTexture() != null) {
            mask |= GL43C.GL_DEPTH_BUFFER_BIT;
        }
        if (target.useStencil) {
            mask |= GL43C.GL_STENCIL_BUFFER_BIT;
        }
        GlDispatch.glBlitFramebuffer(
                0, 0, target.width, target.height,
                0, 0, buffer.width, buffer.height,
                mask,
                GL43C.GL_NEAREST
        );

        GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, 0);
        GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, 0);
        return buffer;
    }

    /**
     * Bind this framebuffer for both reading and writing
     *
     * @param setViewport If true, sets the viewport to match the framebuffer dimensions
     */
    public void bind(boolean setViewport) {
        GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, this.pointer);
        if (setViewport) {
            GlDispatch.glViewport(0, 0, this.width, this.height);
        }
    }

    /**
     * Bind this framebuffer for both reading and writing (with viewport set)
     */
    public void bind() {
        bind(true);
    }

    /**
     * Bind this framebuffer for drawing only
     *
     * @param setViewport If true, sets the viewport to match the framebuffer dimensions
     */
    public void bindDraw(boolean setViewport) {
        GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, this.pointer);
        if (setViewport) {
            GlDispatch.glViewport(0, 0, this.width, this.height);
        }
    }

    /**
     * Bind this framebuffer for reading only
     */
    public void bindRead() {
        GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, this.pointer);
    }

    /**
     * Unbind this framebuffer (binds framebuffer 0)
     */
    public void unbind() {
        GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, 0);
    }

    /**
     * Bind the color texture for reading
     */
    public void bindColorTexture() {
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, colorTexture);
    }

    /**
     * Unbind any 2D texture
     */
    public void unbindColorTexture() {
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, 0);
    }

    /**
     * Clear the framebuffer with the stored clear color
     */
    public void clear() {
        clear(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
    }

    /**
     * Clear the framebuffer with specified color
     */
    public void clear(float r, float g, float b, float a) {
        bind(false);
        GlDispatch.glClearColor(r, g, b, a);
        GlDispatch.glClear(clearMask);
    }

    /**
     * Set the clear color for this framebuffer
     */
    public void setClearColor(float r, float g, float b, float a) {
        this.clearColor[0] = r;
        this.clearColor[1] = g;
        this.clearColor[2] = b;
        this.clearColor[3] = a;
    }

    /**
     * Resize the framebuffer and recreate attachments
     */
    public void resize(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }
        this.width = width;
        this.height = height;
        if (!ownsAttachments) {
            return;
        }

        bind(false);
        deleteAttachments();
        createAttachments();
        unbind();
    }

    /**
     * Get the color texture ID
     */
    public int texture() {
        return colorTexture;
    }

    /**
     * Get the color texture ID (alias for texture())
     */
    public int getColorTexture() {
        return colorTexture;
    }

    /**
     * Get the depth renderbuffer ID
     */
    public int getDepthRenderBuffer() {
        return depthRenderBuffer;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public ResourceLocation id() {
        return id;
    }

    public int getClearMask() {
        return clearMask;
    }

    public int getFilterMode() {
        return filterMode;
    }

    /**
     * Set the texture filter mode for the color attachment
     */
    public void setFilterMode(int filterMode) {
        if (this.filterMode == filterMode) {
            return;
        }
        this.filterMode = filterMode;

        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, colorTexture);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MIN_FILTER, filterMode);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MAG_FILTER, filterMode);
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, 0);
    }

    public boolean hasDepthAttachment() {
        return hasDepth;
    }

    public boolean hasStencilAttachment() {
        return hasStencil;
    }

    /**
     * Create color and depth attachments
     */
    private void createAttachments() {
        // Create color texture
        colorTexture = GlDispatch.glGenTextures();
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, colorTexture);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MIN_FILTER, filterMode);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MAG_FILTER, filterMode);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_WRAP_S, GL43C.GL_CLAMP_TO_EDGE);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_WRAP_T, GL43C.GL_CLAMP_TO_EDGE);
        GlDispatch.glTexImage2D(
                GL43C.GL_TEXTURE_2D,
                0,
                GL43C.GL_RGBA8,
                width,
                height,
                0,
                GL43C.GL_RGBA,
                GL43C.GL_UNSIGNED_BYTE,
                null
        );
        GlDispatch.glFramebufferTexture2D(
                GL43C.GL_FRAMEBUFFER,
                GL43C.GL_COLOR_ATTACHMENT0,
                GL43C.GL_TEXTURE_2D,
                colorTexture,
                0
        );

        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, 0);

        // Create depth (and optionally stencil) renderbuffer
        if (hasDepth) {
            depthRenderBuffer = GlDispatch.glGenRenderbuffers();
            GlDispatch.glBindRenderbuffer(GL43C.GL_RENDERBUFFER, depthRenderBuffer);

            // Use depth24_stencil8 if stencil is needed, otherwise depth24
            int format = hasStencil ? GL43C.GL_DEPTH24_STENCIL8 : GL43C.GL_DEPTH_COMPONENT24;
            GlDispatch.glRenderbufferStorage(GL43C.GL_RENDERBUFFER, format, width, height);

            // Attach depth
            GlDispatch.glFramebufferRenderbuffer(
                    GL43C.GL_FRAMEBUFFER,
                    GL43C.GL_DEPTH_ATTACHMENT,
                    GL43C.GL_RENDERBUFFER,
                    depthRenderBuffer
            );

            // Attach stencil if needed
            if (hasStencil) {
                GlDispatch.glFramebufferRenderbuffer(
                        GL43C.GL_FRAMEBUFFER,
                        GL43C.GL_STENCIL_ATTACHMENT,
                        GL43C.GL_RENDERBUFFER,
                        depthRenderBuffer
                );
            }

            GlDispatch.glBindRenderbuffer(GL43C.GL_RENDERBUFFER, 0);
        }
    }

    /**
     * Delete color and depth attachments
     */
    private void deleteAttachments() {
        if (colorTexture != 0) {
            GlDispatch.glDeleteTextures(colorTexture);
            colorTexture = 0;
        }
        if (depthRenderBuffer != 0) {
            GlDispatch.glDeleteRenderbuffers(depthRenderBuffer);
            depthRenderBuffer = 0;
        }
    }

    private void setDebugLabel(String label) {
        if (label != null && this.pointer != 0) {
            GlDispatch.safeGlObjectLabel(GL43C.GL_FRAMEBUFFER, this.pointer, label);
        }
    }

    @Override
    public void free() {
        if (ownsAttachments) {
            deleteAttachments();
        }
        if (ownsFbo && this.pointer != 0) {
            GlDispatch.glDeleteFramebuffers(this.pointer);
            this.pointer = 0;
        }
    }
}