package io.github.luckymcdev.client.gl.framebuffer;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.OpenGlObject;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;
import org.lwjgl.opengl.GL43C;

public class FrameBuffer extends OpenGlObject {
    private int width;
    private int height;
    private int colorTexture;
    private int depthRenderbuffer;
    private boolean ownsFbo;
    private boolean ownsAttachments;

    public FrameBuffer(int width, int height) {
        this(width, height, true);
    }

    private FrameBuffer(int width, int height, boolean create) {
        this.width = width;
        this.height = height;
        this.ownsFbo = create;
        this.ownsAttachments = create;
        if (create) {
            create();
        }
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

        FrameBuffer buffer = new FrameBuffer(target.width, target.height, true);
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

    public void bind() {
        GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, this.pointer);
    }

    public void unbind() {
        GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, 0);
    }

    public void resize(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }
        this.width = width;
        this.height = height;
        if (!ownsAttachments) {
            return;
        }
        deleteAttachments();
        createAttachments();
    }

    public int texture() {
        return colorTexture;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    private void create() {
        this.set(GlDispatch.glGenFramebuffers());
        bind();
        createAttachments();
        int status = GlDispatch.glCheckFramebufferStatus(GL43C.GL_FRAMEBUFFER);
        if (status != GL43C.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer incomplete: 0x" + Integer.toHexString(status));
        }
        unbind();
    }

    private void createAttachments() {
        colorTexture = GlDispatch.glGenTextures();
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, colorTexture);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MIN_FILTER, GL43C.GL_LINEAR);
        GlDispatch.glTexParameteri(GL43C.GL_TEXTURE_2D, GL43C.GL_TEXTURE_MAG_FILTER, GL43C.GL_LINEAR);
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

        depthRenderbuffer = GlDispatch.glGenRenderbuffers();
        GlDispatch.glBindRenderbuffer(GL43C.GL_RENDERBUFFER, depthRenderbuffer);
        GlDispatch.glRenderbufferStorage(GL43C.GL_RENDERBUFFER, GL43C.GL_DEPTH_COMPONENT24, width, height);
        GlDispatch.glFramebufferRenderbuffer(
            GL43C.GL_FRAMEBUFFER,
            GL43C.GL_DEPTH_ATTACHMENT,
            GL43C.GL_RENDERBUFFER,
            depthRenderbuffer
        );
    }

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
