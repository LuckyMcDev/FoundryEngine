package io.github.luckymcdev.client.render;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.framebuffer.FrameBuffer;
import io.github.luckymcdev.client.gl.shaders.ExtendedShaderType;
import io.github.luckymcdev.client.gl.shaders.Shader;
import io.github.luckymcdev.client.gl.shaders.ShaderSource;
import io.github.luckymcdev.client.gl.shaders.exeption.ShaderException;
import io.github.luckymcdev.client.gl.shaders.program.ShaderProgram;
import io.github.luckymcdev.client.gl.vertex.Mesh;
import io.github.luckymcdev.client.gl.vertex.VertexLayout;
import io.github.luckymcdev.client.gl.vertex.Vertices;
import io.github.luckymcdev.common.Commons;
import io.github.luckymcdev.common.Instances;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43C;
import org.slf4j.Logger;

@EventBusSubscriber
public class TestRender {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static ShaderProgram program;
    private static Mesh quad;
    private static FrameBuffer customBuffer;

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        RenderSystem.assertOnRenderThread();
        ensureInitialized();

        RenderTarget mainTarget = Instances.getMinecraft().getMainRenderTarget();

        // 1. Prepare your custom buffer
        customBuffer.resize(mainTarget.width, mainTarget.height);
        customBuffer.bind();

        // Clear the custom buffer so old frames don't linger
        GL33.glClearColor(0, 0, 0, 0);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

        // 2. Draw your quad WITHOUT blending if you want full opacity
        // or keep it enabled if you want your quad to be semi-transparent
        program.use();
        quad.draw();
        customBuffer.unbind();

        // 3. Blit (Copy) to the main Minecraft target
        // This uses your FrameBuffer's logic to move pixels to the screen
        int mainFbo = getMainFbo();

        GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, customBuffer.pointer());
        GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, mainFbo);

        GlDispatch.glBlitFramebuffer(
                0, 0, customBuffer.width(), customBuffer.height(),
                0, 0, mainTarget.width, mainTarget.height,
                GL43C.GL_COLOR_BUFFER_BIT,
                GL43C.GL_NEAREST
        ); //

        GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, 0);
        GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, 0);
    }

    private static void ensureInitialized() {
        if (program != null && quad != null) {
            return;
        }

        try {
            Shader fragment = new Shader(
                    ExtendedShaderType.FRAGMENT,
                    new ShaderSource(
                            Commons.id("fragment_shader"),
                            Commons.id("shaders/fragmentv1.fsh")
                    )
            );
            Shader vertex = new Shader(
                    ExtendedShaderType.VERTEX,
                    new ShaderSource(
                            Commons.id("vertex_shader"),
                            Commons.id("shaders/vertexv1.fsh")
                    )
            );

            program = new ShaderProgram(Commons.id("test_program"), fragment, vertex);
            program.link();

            if (customBuffer == null) {
                RenderTarget main = Instances.getMinecraft().getMainRenderTarget();
                customBuffer = new FrameBuffer(main.width, main.height);
            }

            int vertexCount = Vertices.TRIANGLE_VERTICES.length / 2;
            quad = new Mesh(Vertices.TRIANGLE_VERTICES, vertexCount, VertexLayout.POS_2D, GL33.GL_TRIANGLES);

            LOGGER.info("Test renderer initialized successfully");
        } catch (ShaderException e) {
            LOGGER.error("Failed to initialize test renderer", e);
            if (e.getGlError() != null) {
                LOGGER.error("OpenGL error: {}", e.getGlError());
            }
            throw new IllegalStateException("Failed to link test shader program", e);
        }
    }

    private static int getMainFbo() {
        GlTexture colorTexture = Instances.getGlTexture();
        GlDevice device = Instances.getGlDevice();
        return colorTexture.getFbo(device.directStateAccess(), null);
    }
}