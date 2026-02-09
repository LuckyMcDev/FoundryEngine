package io.github.luckymcdev.client.render;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.client.gl.GlDispatch;
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
import org.slf4j.Logger;

@EventBusSubscriber
public class TestRender {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static ShaderProgram program;
    private static Mesh quad;

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        RenderSystem.assertOnRenderThread();
        ensureInitialized();

        RenderTarget mainTarget = Instances.getMinecraft().getMainRenderTarget();
        int fbo = getMainFbo(mainTarget);

        // Save current state
        int[] oldViewport = new int[4];
        GL33.glGetIntegerv(GL33.GL_VIEWPORT, oldViewport);

        // Bind framebuffer and set viewport
        GlDispatch.glBindFramebuffer(GL33.GL_FRAMEBUFFER, fbo);
        GlDispatch.glViewport(0, 0, mainTarget.width, mainTarget.height);

        // Enable blending for transparency
        GlDispatch.glEnable(GL33.GL_BLEND);
        GlDispatch.glBlendFuncSeparate(
                GL33.GL_SRC_ALPHA,
                GL33.GL_ONE_MINUS_SRC_ALPHA,
                GL33.GL_ONE,
                GL33.GL_ONE_MINUS_SRC_ALPHA
        );

        // Disable depth testing for 2D overlay
        GlDispatch.glDisable(GL33.GL_DEPTH_TEST);
        GlDispatch.glDepthMask(false);

        // Render the quad
        program.use();
        quad.draw();

        // Restore state
        GlDispatch.glDepthMask(true);
        GlDispatch.glEnable(GL33.GL_DEPTH_TEST);
        GlDispatch.glDisable(GL33.GL_BLEND);

        // Restore viewport if needed
        GlDispatch.glViewport(oldViewport[0], oldViewport[1], oldViewport[2], oldViewport[3]);

        // Unbind to default framebuffer
        GlDispatch.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
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

            int vertexCount = Vertices.QUAD_VERTICES.length / 2;
            quad = new Mesh(Vertices.QUAD_VERTICES, vertexCount, VertexLayout.POS_2D, GL33.GL_TRIANGLES);

            LOGGER.info("Test renderer initialized successfully");
        } catch (ShaderException e) {
            LOGGER.error("Failed to initialize test renderer", e);
            if (e.getGlError() != null) {
                LOGGER.error("OpenGL error: {}", e.getGlError());
            }
            throw new IllegalStateException("Failed to link test shader program", e);
        }
    }

    private static int getMainFbo(RenderTarget target) {
        ValidationGpuTexture colorValidation = (ValidationGpuTexture) target.getColorTexture();
        GlTexture colorTexture = (GlTexture) colorValidation.getRealTexture();
        ValidationGpuDevice deviceValidation = (ValidationGpuDevice) RenderSystem.getDevice();
        GlDevice device = (GlDevice) deviceValidation.getRealDevice();
        return colorTexture.getFbo(device.directStateAccess(), null);
    }
}