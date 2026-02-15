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
import io.github.luckymcdev.foundryengine.client.post.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.client.post.staged.StagedPostProcessPipeline;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class PostProcessManager {
    private static final OpenGlStack GL_STACK = Instances.getOpenGlStack();
    private static final List<PostProcessPipeline> PIPELINES = new ArrayList<>();
    private static final List<PostProcessPipeline> ENABLED_PIPELINES = new ArrayList<>();
    private static final List<StagedPostProcessPipeline> STAGED_PIPELINES = new ArrayList<>();
    private static final List<StagedPostProcessPipeline> ENABLED_STAGED_PIPELINES = new ArrayList<>();

    // Map to organize enabled staged pipelines by their stage
    private static final Map<PostProcessStage, List<StagedPostProcessPipeline>> PIPELINES_BY_STAGE = new EnumMap<>(PostProcessStage.class);

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


    public void addPipeline(StagedPostProcessPipeline pipeline) {
        STAGED_PIPELINES.add(pipeline);
    }

    public void enablePipeline(StagedPostProcessPipeline pipeline) {
        if(STAGED_PIPELINES.contains(pipeline) && !ENABLED_STAGED_PIPELINES.contains(pipeline)) {
            ENABLED_STAGED_PIPELINES.add(pipeline);
            rebuildStageMap();
        }
    }

    public void disablePipeline(StagedPostProcessPipeline pipeline) {
        if(ENABLED_STAGED_PIPELINES.remove(pipeline)) {
            rebuildStageMap();
        }
    }

    /**
     * Rebuilds the map that organizes pipelines by their stage
     */
    private static void rebuildStageMap() {
        PIPELINES_BY_STAGE.clear();
        for (StagedPostProcessPipeline pipeline : ENABLED_STAGED_PIPELINES) {
            PIPELINES_BY_STAGE.computeIfAbsent(pipeline.getStage(), k -> new ArrayList<>()).add(pipeline);
        }
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

    // Event handlers for each stage
    @SubscribeEvent
    public static void onAfterSky(RenderLevelStageEvent.AfterSky event) {
        runStagedPipelines(PostProcessStage.AFTER_SKY);
    }

    @SubscribeEvent
    public static void onAfterOpaqueBlocks(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        runStagedPipelines(PostProcessStage.AFTER_OPAQUE_BLOCKS);
    }

    @SubscribeEvent
    public static void onAfterEntities(RenderLevelStageEvent.AfterEntities event) {
        runStagedPipelines(PostProcessStage.AFTER_ENTITIES);
    }

    @SubscribeEvent
    public static void onAfterTranslucentBlocks(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        runStagedPipelines(PostProcessStage.AFTER_TRANSLUCENT_BLOCKS);
    }

    @SubscribeEvent
    public static void onAfterTripwireBlocks(RenderLevelStageEvent.AfterTripwireBlocks event) {
        runStagedPipelines(PostProcessStage.AFTER_TRIPWIRE_BLOCKS);
    }

    @SubscribeEvent
    public static void onAfterParticles(RenderLevelStageEvent.AfterParticles event) {
        runStagedPipelines(PostProcessStage.AFTER_PARTICLES);
    }

    @SubscribeEvent
    public static void onAfterWeather(RenderLevelStageEvent.AfterWeather event) {
        runStagedPipelines(PostProcessStage.AFTER_WEATHER);
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        runStagedPipelines(PostProcessStage.AFTER_LEVEL);
        // Also run non-staged pipelines at the end
        runNonStagedPipelines();
    }

    /**
     * Run all enabled staged pipelines for a specific stage
     */
    private static void runStagedPipelines(PostProcessStage stage) {
        List<StagedPostProcessPipeline> pipelines = PIPELINES_BY_STAGE.get(stage);
        if (pipelines == null || pipelines.isEmpty()) return;

        runPipelines(pipelines);
    }

    /**
     * Run non-staged pipelines (legacy support)
     */
    private static void runNonStagedPipelines() {
        if (ENABLED_PIPELINES.isEmpty()) return;
        runPipelines(ENABLED_PIPELINES);
    }

    /**
     * Core pipeline execution logic
     */
    private static void runPipelines(List<? extends PostProcessPipeline> pipelines) {
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

        GlDispatch.pushDebugGroup("Post-Process Pass (" + pipelines.size() + " pipelines)");

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

            for (PostProcessPipeline pipeline : pipelines) {
                // On first pass, bind the main target's color texture
                // On subsequent passes, bind the previous output
                GlDispatch.pushDebugGroup("Pipeline: " + pipeline.getProgram().getId());
                try {
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
                } finally {
                    GlDispatch.popDebugGroup();
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
            GlDispatch.popDebugGroup();
            GL_STACK.pop();
        }
    }

    public List<PostProcessPipeline> getPipelines() {
        return PIPELINES;
    }

    public List<PostProcessPipeline> getEnabledPipelines() {
        return ENABLED_PIPELINES;
    }

    public List<StagedPostProcessPipeline> getStagedPipelines() {
        return STAGED_PIPELINES;
    }

    public List<StagedPostProcessPipeline> getEnabledStagedPipelines() {
        return ENABLED_STAGED_PIPELINES;
    }

    public Map<PostProcessStage, List<StagedPostProcessPipeline>> getPipelinesByStage() {
        return PIPELINES_BY_STAGE;
    }

    private static void setupGlobalState() {
        GlDispatch.glDisable(GL43C.GL_DEPTH_TEST);
        GlDispatch.glDisable(GL43C.GL_BLEND);
        GlDispatch.glDisable(GL43C.GL_CULL_FACE);
        GlDispatch.glDepthMask(false);
    }
}