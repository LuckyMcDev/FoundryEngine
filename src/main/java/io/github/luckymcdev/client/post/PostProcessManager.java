package io.github.luckymcdev.client.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.client.RegisterRenderingStuffEvent;
import io.github.luckymcdev.client.opengl.GlDispatch;
import io.github.luckymcdev.client.opengl.OpenGlStack;
import io.github.luckymcdev.client.opengl.framebuffer.FrameBuffer;
import io.github.luckymcdev.client.opengl.vertex.Mesh;
import io.github.luckymcdev.client.opengl.vertex.VertexLayout;
import io.github.luckymcdev.client.opengl.vertex.Vertices;
import io.github.luckymcdev.common.Commons;
import io.github.luckymcdev.common.Instances;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class PostProcessManager {
    private static final OpenGlStack GL_STACK = Instances.getOpenGlStack();
    private static final List<PostProcessPipeline> PIPELINES = new ArrayList<>();
    private static final List<PostProcessPipeline> ENABLED_PIPELINES = new ArrayList<>();

    private static Mesh quad;
    private static FrameBuffer bufferPing;
    private static FrameBuffer bufferPong;

    /**
     * Register a post-processing pipeline.
     * Should be called during mod initialization, NOT during rendering.
     */
    public void addPipeline(PostProcessPipeline pipeline) {
        PIPELINES.add(pipeline);
    }

    public void enablePipeline(PostProcessPipeline pipeline) {
        if(PIPELINES.contains(pipeline)) {
            ENABLED_PIPELINES.add(pipeline);
        }
    }

    public void disablePipeline(PostProcessPipeline pipeline) {
        if(PIPELINES.contains(pipeline) && ENABLED_PIPELINES.contains(pipeline)) {
            ENABLED_PIPELINES.remove(pipeline);
        }
    }

    public List<PostProcessPipeline> getEnabledPipelines() {
        return ENABLED_PIPELINES;
    }

    public List<PostProcessPipeline> getPipelines() {
        return PIPELINES;
    }

    @SubscribeEvent
    private static void init(RegisterRenderingStuffEvent event) {
        RenderTarget main = Instances.getMainRenderTarget();
        bufferPing = new FrameBuffer(Commons.id("post_buffer_ping"), main.width, main.height);
        bufferPong = new FrameBuffer(Commons.id("post_buffer_pong"), main.width, main.height);

        quad = new Mesh(
                Vertices.FULLSCREEN_QUAD.vertices(),
                Vertices.FULLSCREEN_QUAD.indices(),
                VertexLayout.POS_TEX_2D,
                GL33.GL_TRIANGLES,
                true
        );
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterLevel event) {
        if (PIPELINES.isEmpty()) return;
        RenderSystem.assertOnRenderThread();

        RenderTarget mainTarget = Instances.getMainRenderTarget();
        ensureBufferSize(mainTarget);

        GL_STACK.push();
        setupGlobalState();

        // Start Ping-Ponging
        FrameBuffer currentInput = null;
        FrameBuffer currentOutput = bufferPing;

        for (int i = 0; i < ENABLED_PIPELINES.size(); i++) {
            PostProcessPipeline pipeline = ENABLED_PIPELINES.get(i);

            // 1. Determine Input Texture ID
            int inputTexId = (i == 0)
                    ? Instances.getGlColTexture(mainTarget).glId()
                    : currentInput.getColorTexture();

            // 2. Bind Input Texture
            GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0);
            GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, inputTexId);

            // 3. Prepare Output
            currentOutput.bind();
            GlDispatch.glViewport(0, 0, currentOutput.width(), currentOutput.height());
            GlDispatch.glClearColor(0, 0, 0, 0);
            GlDispatch.glClear(GL43C.GL_COLOR_BUFFER_BIT);

            // 4. Draw with the specific shader
            pipeline.getProgram().use();
            pipeline.setupDefaultUniforms();
            pipeline.setupUniforms();
            quad.draw();
            pipeline.getProgram().disable();

            // 5. Cleanup
            GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, 0);
            currentOutput.unbind();

            // 6. Swap buffers for next pass
            currentInput = currentOutput;
            currentOutput = (currentOutput == bufferPing) ? bufferPong : bufferPing;
        }

        // Final Blit: The last 'currentInput' contains the finished result
        if(currentInput != null) {
            int mainFbo = Instances.getGlColTexture().getFbo(Instances.getGlDevice().directStateAccess(), null);
            Instances.getFrameBufferManager().blit(currentInput, mainFbo, mainTarget);
        }

        GL_STACK.pop();
    }

    private static void ensureBufferSize(RenderTarget main) {
        if (bufferPing.width() != main.width || bufferPing.height() != main.height) {
            bufferPing.resize(main.width, main.height);
            bufferPong.resize(main.width, main.height);
        }
    }

    private static void setupGlobalState() {
        GlDispatch.glDisable(GL43C.GL_DEPTH_TEST);
        GlDispatch.glDisable(GL43C.GL_BLEND);
        GlDispatch.glDisable(GL43C.GL_CULL_FACE);
        GlDispatch.glDepthMask(false);
    }
}