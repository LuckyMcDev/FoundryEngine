package io.github.luckymcdev.client.gl.framebuffer;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.OpenGlObject;
import io.github.luckymcdev.common.Commons;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL43C;

import java.util.Map;

public class FrameBuffer extends OpenGlObject {

    // Human-readable error messages for framebuffer status
    protected static final Map<Integer, String> ERRORS = Map.of(
            GL43C.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT, "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT",
            GL43C.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT, "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT",
            GL43C.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER, "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER",
            GL43C.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER, "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER",
            GL43C.GL_FRAMEBUFFER_UNSUPPORTED, "GL_FRAMEBUFFER_UNSUPPORTED",
            GL43C.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE, "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE",
            GL43C.GL_FRAMEBUFFER_UNDEFINED, "GL_FRAMEBUFFER_UNDEFINED",
            GL43C.GL_OUT_OF_MEMORY, "GL_OUT_OF_MEMORY"
    );

    private final ResourceLocation id;
    private int width;
    private int height;
    private int colorTexture;
    private int depthRenderbuffer;
    private final boolean ownsFbo;
    private final boolean ownsAttachments;
    private final boolean hasDepth;
    private final boolean hasStencil;
    private final String debugLabel;
    private final int clearMask;
    private int filterMode = GL43C.GL_LINEAR;
    private float[] clearColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f};

    public FrameBuffer(ResourceLocation id, int width, int height) {
        this(id, width, height, true, false);
    }

    public FrameBuffer(ResourceLocation id, int width, int height, boolean useDepth) {
        this(id, width, height, useDepth, false);
    }

    public FrameBuffer(ResourceLocation id, int width, int height, boolean useDepth, boolean useStencil) {
        this(id, width, height, useDepth, useStencil, null);
    }

    public FrameBuffer(ResourceLocation id, int width, int height, boolean useDepth, boolean useStencil, @Nullable String debugLabel) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.ownsFbo = true;
        this.ownsAttachments = true;
        this.hasDepth = useDepth;
        this.hasStencil = useStencil && useDepth; // Stencil requires depth
        this.debugLabel = debugLabel != null ? debugLabel : id.toString();

        // Calculate clear mask based on attachments
        int mask = GL43C.GL_COLOR_BUFFER_BIT;
        if (this.hasDepth) {
            mask |= GL43C.GL_DEPTH_BUFFER_BIT;
        }
        if (this.hasStencil) {
            mask |= GL43C.GL_STENCIL_BUFFER_BIT;
        }
        this.clearMask = mask;

        create();
    }

    public static FrameBuffer fromTarget(RenderTarget target) {
        RenderSystem.assertOnRenderThread();
        if (target.getColorTexture() == null) {
            throw new IllegalArgumentException("RenderTarget has no color texture");
        }
        ValidationGpuTexture colorValidation = (ValidationGpuTexture) target.getColorTexture();
        GlTexture colorTexture = (GlTexture) colorValidation.getRealTexture();
        GlTexture depthTexture = null;
        if (target.getDepthTexture() != null) {
            ValidationGpuTexture depthValidation = (ValidationGpuTexture) target.getDepthTexture();
            depthTexture = (GlTexture) depthValidation.getRealTexture();
        }
        ValidationGpuDevice deviceValidation = (ValidationGpuDevice) RenderSystem.getDevice();
        GlDevice device = (GlDevice) deviceValidation.getRealDevice();

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
        checkFramebufferStatus("after resize");
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
    public int getDepthRenderbuffer() {
        return depthRenderbuffer;
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

    public String getDebugLabel() {
        return debugLabel;
    }

    public int getClearMask() {
        return clearMask;
    }

    public int getFilterMode() {
        return filterMode;
    }

    public boolean hasDepthAttachment() {
        return hasDepth;
    }

    public boolean hasStencilAttachment() {
        return hasStencil;
    }

    /**
     * Create the framebuffer and attachments
     */
    private void create() {
        this.set(GlDispatch.glGenFramebuffers());

        // Set debug label if available (requires GL 4.3+)
        if (debugLabel != null) {
            try {
                GlDispatch.glObjectLabel(GL43C.GL_FRAMEBUFFER, this.pointer, debugLabel);
            } catch (Exception e) {
                // Ignore if not supported
            }
        }

        bind(false);
        createAttachments();
        checkFramebufferStatus("after creation");
        unbind();
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

        // Set debug label for texture
        if (debugLabel != null) {
            try {
                GlDispatch.glObjectLabel(GL43C.GL_TEXTURE, colorTexture, debugLabel + " / Color");
            } catch (Exception e) {
                // Ignore if not supported
            }
        }

        // Create depth (and optionally stencil) renderbuffer
        if (hasDepth) {
            depthRenderbuffer = GlDispatch.glGenRenderbuffers();
            GlDispatch.glBindRenderbuffer(GL43C.GL_RENDERBUFFER, depthRenderbuffer);

            // Use depth24_stencil8 if stencil is needed, otherwise depth24
            int format = hasStencil ? GL43C.GL_DEPTH24_STENCIL8 : GL43C.GL_DEPTH_COMPONENT24;
            GlDispatch.glRenderbufferStorage(GL43C.GL_RENDERBUFFER, format, width, height);

            // Attach depth
            GlDispatch.glFramebufferRenderbuffer(
                    GL43C.GL_FRAMEBUFFER,
                    GL43C.GL_DEPTH_ATTACHMENT,
                    GL43C.GL_RENDERBUFFER,
                    depthRenderbuffer
            );

            // Attach stencil if needed
            if (hasStencil) {
                GlDispatch.glFramebufferRenderbuffer(
                        GL43C.GL_FRAMEBUFFER,
                        GL43C.GL_STENCIL_ATTACHMENT,
                        GL43C.GL_RENDERBUFFER,
                        depthRenderbuffer
                );
            }

            // Set debug label for renderbuffer
            if (debugLabel != null) {
                try {
                    String label = hasStencil ? debugLabel + " / Depth+Stencil" : debugLabel + " / Depth";
                    GlDispatch.glObjectLabel(GL43C.GL_RENDERBUFFER, depthRenderbuffer, label);
                } catch (Exception e) {
                    // Ignore if not supported
                }
            }
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
        if (depthRenderbuffer != 0) {
            GlDispatch.glDeleteRenderbuffers(depthRenderbuffer);
            depthRenderbuffer = 0;
        }
    }

    /**
     * Check framebuffer status and throw detailed exception if incomplete
     */
    private void checkFramebufferStatus(String context) {
        int status = GlDispatch.glCheckFramebufferStatus(GL43C.GL_FRAMEBUFFER);
        if (status != GL43C.GL_FRAMEBUFFER_COMPLETE) {
            String errorName = ERRORS.getOrDefault(status, "UNKNOWN_ERROR");
            throw new IllegalStateException(
                    String.format("Framebuffer '%s' incomplete %s: %s (0x%x)",
                            debugLabel, context, errorName, status)
            );
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