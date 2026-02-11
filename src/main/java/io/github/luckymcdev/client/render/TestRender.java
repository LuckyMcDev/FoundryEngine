package io.github.luckymcdev.client.render;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.client.TbRenderSystem;
import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.OpenGlStack;
import io.github.luckymcdev.client.gl.framebuffer.FrameBuffer;
import io.github.luckymcdev.client.gl.shaders.ExtendedShaderType;
import io.github.luckymcdev.client.gl.shaders.Shader;
import io.github.luckymcdev.client.gl.shaders.ShaderSource;
import io.github.luckymcdev.client.gl.shaders.exeption.ShaderException;
import io.github.luckymcdev.client.gl.shaders.program.ShaderProgram;
import io.github.luckymcdev.client.gl.shaders.uniform.Uniform;
import io.github.luckymcdev.client.gl.vertex.Mesh;
import io.github.luckymcdev.client.gl.vertex.VertexLayout;
import io.github.luckymcdev.client.gl.vertex.Vertices;
import io.github.luckymcdev.common.Commons;
import io.github.luckymcdev.common.Instances;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    private static final OpenGlStack glStack = new OpenGlStack();

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        RenderSystem.assertOnRenderThread();
        ensureInitialized();

        RenderTarget mainTarget = Instances.getMainRenderTarget();

        // Ensure buffer matches main target size
        if (customBuffer.width() != mainTarget.width || customBuffer.height() != mainTarget.height) {
            customBuffer.resize(mainTarget.width, mainTarget.height);
        }

        // Save OpenGL state
        glStack.push();

        try {
            // Setup post-processing state
            GlDispatch.glDisable(GL43C.GL_DEPTH_TEST);
            GlDispatch.glDisable(GL43C.GL_BLEND);
            GlDispatch.glDisable(GL43C.GL_CULL_FACE);
            GlDispatch.glDepthMask(false);

            // 1. Bind Input (The Game Screen)
            GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0);
            int inputTextureId = Instances.getGlTexture(mainTarget).glId();
            GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, inputTextureId);

            // 2. Prepare Output (Your Buffer)
            customBuffer.bind();
            GlDispatch.glClearColor(0, 0, 0, 0);
            GlDispatch.glClear(GL43C.GL_COLOR_BUFFER_BIT);

            // 3. Draw
            program.use();
            program.setUniform(new Uniform<>("screenTexture", 0));
            quad.draw();

            program.disable();
            GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, 0);
            customBuffer.unbind();

            int mainFbo = getMainFbo();
            TbRenderSystem.renderer().getFrameBufferManager().blit(customBuffer, mainFbo, mainTarget);
        } finally {
            glStack.pop();
        }
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
                customBuffer = new FrameBuffer(Commons.id("custombuffer"), main.width, main.height);
            }

            quad = new Mesh(
                    Vertices.FULLSCREEN_QUAD.vertices(),
                    Vertices.FULLSCREEN_QUAD.indices(),
                    VertexLayout.POS_TEX_2D,
                    GL33.GL_TRIANGLES,
                    true
            );

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