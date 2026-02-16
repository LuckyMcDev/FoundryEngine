package io.github.luckymcdev.foundryengine.client.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.foundryengine.client.RegisterRenderingStuffEvent;
import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlStack;
import io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBuffer;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.opengl.vertex.Mesh;
import io.github.luckymcdev.foundryengine.client.opengl.vertex.VertexLayout;
import io.github.luckymcdev.foundryengine.client.opengl.vertex.Vertices;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.StagedPostProcessPipeline;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43C;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EventBusSubscriber
public class PostProcessManager {
    private static final OpenGlStack GL_STACK = Instances.getOpenGlStack();
    private static final List<PostProcessPipeline> PIPELINES = new ArrayList<>();
    private static final List<StagedPostProcessPipeline> STAGED_PIPELINES = new ArrayList<>();
    private static final int COLOR_TEXTURE_UNIT = 0;
    private static final int DEPTH_TEXTURE_UNIT = 1;
    private static final int SAVED_TEXTURE_UNIT_COUNT = 8;
    private static final PostProcessStage STAGE_NOT_STAGED = null;

    private static Mesh quad;
    private static FrameBuffer bufferPing;

    public void addPipeline(PostProcessPipeline pipeline) {
        PIPELINES.add(pipeline);
        registerPipelineProgram(pipeline.getProgram());
    }

    public void enablePipeline(PostProcessPipeline pipeline) {
        if (PIPELINES.contains(pipeline)) {
            pipeline.enable();
        }
    }

    public void disablePipeline(PostProcessPipeline pipeline) {
        pipeline.disable();
    }

    public void addPipeline(StagedPostProcessPipeline pipeline) {
        STAGED_PIPELINES.add(pipeline);
        registerPipelineProgram(pipeline.getProgram());
    }

    public void enablePipeline(StagedPostProcessPipeline pipeline) {
        if (STAGED_PIPELINES.contains(pipeline) && !pipeline.isEnabled()) {
            pipeline.enable();
        }
    }

    public void disablePipeline(StagedPostProcessPipeline pipeline) {
        pipeline.disable();
    }

    private static void registerPipelineProgram(ShaderProgram program) {
        Instances.getShaderManager().register(program);
        program.shaders().forEach(Instances.getShaderManager()::register);
    }

    @SubscribeEvent
    private static void init(RegisterRenderingStuffEvent event) {
        RenderTarget main = Instances.getMainRenderTarget();
        bufferPing = new FrameBuffer(Commons.id("post_buffer_ping"), main.width, main.height, false, false);

        quad = new Mesh(
                Vertices.FULLSCREEN_QUAD.vertices(),
                Vertices.FULLSCREEN_QUAD.indices(),
                VertexLayout.POS_TEX_2D,
                GL33.GL_TRIANGLES,
                true
        );
    }

    // Event handlers for each stage
    @SubscribeEvent
    public static void onAfterSky(RenderLevelStageEvent.AfterSky event) {
        runStage(PostProcessStage.AFTER_SKY);
    }

    @SubscribeEvent
    public static void onAfterOpaqueBlocks(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        runStage(PostProcessStage.AFTER_OPAQUE_BLOCKS);
    }

    @SubscribeEvent
    public static void onAfterEntities(RenderLevelStageEvent.AfterEntities event) {
        runStage(PostProcessStage.AFTER_ENTITIES);
    }

    @SubscribeEvent
    public static void onAfterTranslucentBlocks(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        runStage(PostProcessStage.AFTER_TRANSLUCENT_BLOCKS);
    }

    @SubscribeEvent
    public static void onAfterTripwireBlocks(RenderLevelStageEvent.AfterTripwireBlocks event) {
        runStage(PostProcessStage.AFTER_TRIPWIRE_BLOCKS);
    }

    @SubscribeEvent
    public static void onAfterParticles(RenderLevelStageEvent.AfterParticles event) {
        runStage(PostProcessStage.AFTER_PARTICLES);
    }

    @SubscribeEvent
    public static void onAfterWeather(RenderLevelStageEvent.AfterWeather event) {
        runStage(PostProcessStage.AFTER_WEATHER);
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        runStage(PostProcessStage.AFTER_LEVEL);
        runNonStagedPipelines();
    }

    @SubscribeEvent
    public static void onAfterRender(RenderGuiEvent.Post event) {
    }

    private static void runStage(PostProcessStage stage) {
        runStagedPipelines(stage);
    }

    /**
     * Run all enabled staged pipelines for a specific stage
     */
    private static void runStagedPipelines(PostProcessStage stage) {
        List<StagedPostProcessPipeline> pipelines = getEnabledPipelines(
                STAGED_PIPELINES,
                pipeline -> pipeline.getStage() == stage
        );
        if (pipelines.isEmpty()) return;
        runPipelineBatch(pipelines, stage);
    }

    /**
     * Run non-staged pipelines (legacy support)
     */
    private static void runNonStagedPipelines() {
        List<PostProcessPipeline> pipelines = getEnabledPipelines(PIPELINES, p -> true);
        if (pipelines.isEmpty()) return;
        runPipelineBatch(pipelines, STAGE_NOT_STAGED);
    }

    private static <T extends PostProcessPipeline> List<T> getEnabledPipelines(
            List<T> pipelines,
            java.util.function.Predicate<T> extraFilter
    ) {
        return pipelines.stream()
                .filter(PostProcessPipeline::isEnabled)
                .filter(extraFilter)
                .collect(Collectors.toList());
    }

    /**
     * Core pipeline execution logic
     * Each pipeline: read from main -> render to temp -> blit back to main
     */
    private static void runPipelineBatch(List<? extends PostProcessPipeline> pipelines, PostProcessStage stage) {
        RenderSystem.assertOnRenderThread();

        RenderTarget mainTarget = Instances.getMainRenderTarget();
        ensureBuffers(mainTarget);
        GlDispatch.pushDebugGroup(buildDebugGroupLabel(pipelines.size(), stage));

        GL_STACK.push();
        GlStateSnapshot saved = GlStateSnapshot.capture();
        try {
            setupGlobalState();

            var colorTexture = Instances.getGlColTexture();
            int mainColorTextureId = colorTexture.glId();
            int depthTextureId = Instances.getGlDepthTexture().glId();
            var device = Instances.getGlDevice();
            int mainFbo = colorTexture.getFbo(device.directStateAccess(), null);

            for (PostProcessPipeline pipeline : pipelines) {
                runPipeline(pipeline, mainTarget, mainFbo, mainColorTextureId, depthTextureId);
            }
        } finally {
            saved.restore();
            GlDispatch.popDebugGroup();
            GL_STACK.pop();
        }
    }

    /**
     * Change the stage of a staged pipeline at runtime
     */
    public void changePipelineStage(StagedPostProcessPipeline pipeline, PostProcessStage newStage) {
        if (STAGED_PIPELINES.contains(pipeline)) {
            pipeline.setStage(newStage);
        }
    }

    public List<PostProcessPipeline> getPipelines() {
        return PIPELINES;
    }

    public List<PostProcessPipeline> getEnabledPipelines() {
        return PIPELINES.stream()
                .filter(PostProcessPipeline::isEnabled)
                .collect(Collectors.toList());
    }

    public List<StagedPostProcessPipeline> getStagedPipelines() {
        return STAGED_PIPELINES;
    }

    public List<StagedPostProcessPipeline> getEnabledStagedPipelines() {
        return STAGED_PIPELINES.stream()
                .filter(StagedPostProcessPipeline::isEnabled)
                .collect(Collectors.toList());
    }

    public Map<PostProcessStage, List<StagedPostProcessPipeline>> getPipelinesByStage() {
        EnumMap<PostProcessStage, List<StagedPostProcessPipeline>> pipelinesByStage = initPipelinesByStage();
        for (StagedPostProcessPipeline pipeline : STAGED_PIPELINES) {
            pipelinesByStage
                    .computeIfAbsent(pipeline.getStage(), stage -> new ArrayList<>())
                    .add(pipeline);
        }
        return pipelinesByStage;
    }

    private static EnumMap<PostProcessStage, List<StagedPostProcessPipeline>> initPipelinesByStage() {
        EnumMap<PostProcessStage, List<StagedPostProcessPipeline>> pipelinesByStage =
                new EnumMap<>(PostProcessStage.class);
        for (PostProcessStage stage : PostProcessStage.values()) {
            pipelinesByStage.put(stage, new ArrayList<>());
        }
        return pipelinesByStage;
    }

    private static void setupGlobalState() {
        GlDispatch.glDisable(GL43C.GL_DEPTH_TEST);
        GlDispatch.glDisable(GL43C.GL_BLEND);
        GlDispatch.glDisable(GL43C.GL_CULL_FACE);
        GlDispatch.glDepthMask(false);
    }

    private static void ensureBuffers(RenderTarget mainTarget) {
        bufferPing = resizeOrCreate(bufferPing, "post_buffer_ping", mainTarget);
    }

    private static FrameBuffer resizeOrCreate(FrameBuffer buffer, String id, RenderTarget mainTarget) {
        if (buffer == null || buffer.width() != mainTarget.width || buffer.height() != mainTarget.height) {
            if (buffer != null) buffer.free();
            return new FrameBuffer(Commons.id(id), mainTarget.width, mainTarget.height, false, false);
        }
        return buffer;
    }

    private static String buildDebugGroupLabel(int pipelineCount, PostProcessStage stage) {
        String stageLabel = (stage == null) ? "UNSTAGED" : stage.name();
        return "Post-Process Pass (" + pipelineCount + " pipelines, stage: " + stageLabel + ")";
    }

    private static void runPipeline(
            PostProcessPipeline pipeline,
            RenderTarget mainTarget,
            int mainFbo,
            int mainColorTextureId,
            int depthTextureId
    ) {
        List<ShaderProgram> passes = pipeline.getPasses();
        GlDispatch.pushDebugGroup("Pipeline: " + pipeline.getName());

        for (int i = 0; i < passes.size(); i++) {
            ShaderProgram program = passes.get(i);
            renderPass(pipeline, program, i, bufferPing, mainColorTextureId, depthTextureId);
            Instances.getFrameBufferManager().blit(bufferPing, mainFbo, mainTarget);
        }

        GlDispatch.popDebugGroup();
    }

    private static void renderPass(
            PostProcessPipeline pipeline,
            ShaderProgram program,
            int passIndex,
            FrameBuffer outputBuffer,
            int mainColorTextureId,
            int depthTextureId
    ) {
        GlDispatch.pushDebugGroup("Pass " + passIndex + ": " + program.getId());

        outputBuffer.bind();
        GlDispatch.glClear(GL43C.GL_COLOR_BUFFER_BIT);

        GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + COLOR_TEXTURE_UNIT);
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, mainColorTextureId);

        GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + DEPTH_TEXTURE_UNIT);
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, depthTextureId);

        program.use();
        pipeline.setupDefaultUniforms(program);
        pipeline.setupUniforms(passIndex);
        quad.draw();
        program.disable();
        outputBuffer.unbind();

        GlDispatch.popDebugGroup();
    }

    private record GlStateSnapshot(int readFbo, int drawFbo, int activeTexture, int[] textureBindings) {

        private static GlStateSnapshot capture() {
                int readFbo = GlDispatch.glGetInteger(GL43C.GL_READ_FRAMEBUFFER_BINDING);
                int drawFbo = GlDispatch.glGetInteger(GL43C.GL_DRAW_FRAMEBUFFER_BINDING);
                int activeTexture = GlDispatch.glGetInteger(GL43C.GL_ACTIVE_TEXTURE);
                int[] textureBindings = new int[SAVED_TEXTURE_UNIT_COUNT];
                for (int i = 0; i < SAVED_TEXTURE_UNIT_COUNT; i++) {
                    GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + i);
                    textureBindings[i] = GlDispatch.glGetInteger(GL43C.GL_TEXTURE_BINDING_2D);
                }
                return new GlStateSnapshot(readFbo, drawFbo, activeTexture, textureBindings);
        }

        private void restore() {
            GlDispatch.glBindFramebuffer(GL43C.GL_READ_FRAMEBUFFER, readFbo);
            GlDispatch.glBindFramebuffer(GL43C.GL_DRAW_FRAMEBUFFER, drawFbo);
            GlDispatch.glBindFramebuffer(GL43C.GL_FRAMEBUFFER, drawFbo);

            for (int i = 0; i < textureBindings.length; i++) {
                GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + i);
                GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, textureBindings[i]);
            }
            GlDispatch.glActiveTexture(activeTexture);
        }
    }
}