package io.github.luckymcdev.foundryengine.client.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.foundryengine.client.RegisterRenderingStuffEvent;
import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlStack;
import io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBuffer;
import io.github.luckymcdev.foundryengine.client.opengl.vertex.Mesh;
import io.github.luckymcdev.foundryengine.client.opengl.vertex.VertexLayout;
import io.github.luckymcdev.foundryengine.client.opengl.vertex.Vertices;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    private static int lastMainWidth = -1;
    private static int lastMainHeight = -1;

    public void addPipeline(PostProcessPipeline pipeline) {
        PIPELINES.add(pipeline);
    }

    public void enablePipeline(PostProcessPipeline pipeline) {
        if(PIPELINES.contains(pipeline)) {
            ENABLED_PIPELINES.add(pipeline);
        }
    }

    public void disablePipeline(PostProcessPipeline pipeline) {
        ENABLED_PIPELINES.remove(pipeline);
    }

    @SubscribeEvent
    private static void init(RegisterRenderingStuffEvent event) {
        RenderTarget main = Instances.getMainRenderTarget();
        bufferPing = new FrameBuffer(Commons.id("post_buffer_ping"), main.width, main.height, false, false);
        bufferPong = new FrameBuffer(Commons.id("post_buffer_pong"), main.width, main.height, false, false);

        quad = new Mesh(
                Vertices.FULLSCREEN_QUAD.vertices(),
                Vertices.FULLSCREEN_QUAD.indices(),
                VertexLayout.POS_TEX_2D,
                GL33.GL_TRIANGLES,
                true
        );

        lastMainWidth = main.width;
        lastMainHeight = main.height;
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterLevel event) {
        if (ENABLED_PIPELINES.isEmpty()) return;
        RenderSystem.assertOnRenderThread();

        RenderTarget mainTarget = Instances.getMainRenderTarget();

        // Resize buffers if main target size changed
        if (bufferPing == null || lastMainWidth != mainTarget.width || lastMainHeight != mainTarget.height) {
            if (bufferPing != null) bufferPing.free();
            bufferPing = new FrameBuffer(Commons.id("post_buffer_ping"), mainTarget.width, mainTarget.height, false, false);
            lastMainWidth = mainTarget.width;
            lastMainHeight = mainTarget.height;
        }

        if (bufferPong.width() != mainTarget.width || bufferPong.height() != mainTarget.height) {
            bufferPong.resize(mainTarget.width, mainTarget.height);
        }

        GL_STACK.push();
        try {
            // Save initial state
            int savedReadFbo = GlDispatch.glGetInteger(GL43C.GL_READ_FRAMEBUFFER_BINDING);
            int savedDrawFbo = GlDispatch.glGetInteger(GL43C.GL_DRAW_FRAMEBUFFER_BINDING);
            int savedActiveTexture = GlDispatch.glGetInteger(GL43C.GL_ACTIVE_TEXTURE);
            int[] savedTextureBindings = new int[8];
            for (int i = 0; i < 8; i++) {
                GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + i);
                savedTextureBindings[i] = GlDispatch.glGetInteger(GL43C.GL_TEXTURE_BINDING_2D);
            }

            setupGlobalState();

            FrameBuffer currentInput = bufferPing;
            FrameBuffer currentOutput = bufferPong;
            boolean isFirstPass = true;

            for (PostProcessPipeline pipeline : ENABLED_PIPELINES) {
                // On first pass, bind the main target's color texture
                // On subsequent passes, bind the previous output
                GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0);
                if (isFirstPass) {
                    var colorTexture = Instances.getGlColTexture();
                    GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, colorTexture.glId());
                } else {
                    currentInput.bindColorTexture();
                }

                // Always bind the main target's depth texture
                GlDispatch.glActiveTexture(GL43C.GL_TEXTURE1);
                var depthTexture = Instances.getGlDepthTexture();
                if (depthTexture != null) {
                    GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, depthTexture.glId());
                }

                currentOutput.bind();
                GlDispatch.glClear(GL43C.GL_COLOR_BUFFER_BIT);

                pipeline.getProgram().use();
                pipeline.setupDefaultUniforms();
                pipeline.setupUniforms();
                quad.draw();
                pipeline.getProgram().disable();

                currentOutput.unbind();

                // After first pass, swap to ping-pong buffers
                if (isFirstPass) {
                    currentInput = currentOutput;
                    currentOutput = currentInput == bufferPing ? bufferPong : bufferPing;
                    isFirstPass = false;
                } else {
                    FrameBuffer temp = currentInput;
                    currentInput = currentOutput;
                    currentOutput = temp;
                }
            }

            // Blit final result to main framebuffer
            var colorTexture = Instances.getGlColTexture();
            var device = Instances.getGlDevice();
            int mainFbo = colorTexture.getFbo(device.directStateAccess(), null);
            Instances.getFrameBufferManager().blit(currentInput, mainFbo, mainTarget);

            // Restore framebuffer state
            GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, savedReadFbo);
            GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, savedDrawFbo);
            GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, savedDrawFbo);

            // Restore texture unit and bindings
            for (int i = 0; i < 8; i++) {
                GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + i);
                GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, savedTextureBindings[i]);
            }
            GlDispatch.glActiveTexture(savedActiveTexture);
        } finally {
            GL_STACK.pop();
        }
    }

    public List<PostProcessPipeline> getPipelines() {
        return PIPELINES;
    }

    public List<PostProcessPipeline> getEnabledPipelines() {
        return ENABLED_PIPELINES;
    }

    private static void setupGlobalState() {
        GlDispatch.glDisable(GL43C.GL_DEPTH_TEST);
        GlDispatch.glDisable(GL43C.GL_BLEND);
        GlDispatch.glDisable(GL43C.GL_CULL_FACE);
        GlDispatch.glDepthMask(false);
    }
}