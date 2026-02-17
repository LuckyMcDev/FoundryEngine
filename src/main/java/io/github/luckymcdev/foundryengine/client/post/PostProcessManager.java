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
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PassTarget;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
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

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final OpenGlStack GL_STACK = Instances.getOpenGlStack();
    private static final int COLOR_TEXTURE_UNIT = 0;
    private static final int DEPTH_TEXTURE_UNIT = 1;
    private static final int SAVED_TEXTURE_UNIT_COUNT = 8;
    private static final PostProcessStage STAGE_NOT_STAGED = null;

    // -------------------------------------------------------------------------
    // Pipeline registries
    // -------------------------------------------------------------------------

    private static final List<PostProcessPipeline> PIPELINES = new ArrayList<>();
    private static final List<StagedPostProcessPipeline> STAGED_PIPELINES = new ArrayList<>();

    /**
     * Holds all manager-owned scratch {@link FrameBuffer}s.
     * Only {@link PassTarget#isManagerOwned()} targets are stored here.
     */
    private static final EnumMap<PassTarget, FrameBuffer> SCRATCH_BUFFERS =
            new EnumMap<>(PassTarget.class);

    private static Mesh quad;

    public void addPipeline(PostProcessPipeline pipeline) {
        PIPELINES.add(pipeline);
        registerPipelinePrograms(pipeline);
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
        registerPipelinePrograms(pipeline);
    }

    public void enablePipeline(StagedPostProcessPipeline pipeline) {
        if (STAGED_PIPELINES.contains(pipeline) && !pipeline.isEnabled()) {
            pipeline.enable();
        }
    }

    public void disablePipeline(StagedPostProcessPipeline pipeline) {
        pipeline.disable();
    }

    public void changePipelineStage(StagedPostProcessPipeline pipeline, PostProcessStage newStage) {
        if (STAGED_PIPELINES.contains(pipeline)) {
            pipeline.setStage(newStage);
        }
    }

    public List<PostProcessPipeline> getPipelines() { return PIPELINES; }

    public List<PostProcessPipeline> getEnabledPipelines() {
        return PIPELINES.stream().filter(PostProcessPipeline::isEnabled).collect(Collectors.toList());
    }

    public List<StagedPostProcessPipeline> getStagedPipelines() { return STAGED_PIPELINES; }

    public List<StagedPostProcessPipeline> getEnabledStagedPipelines() {
        return STAGED_PIPELINES.stream().filter(StagedPostProcessPipeline::isEnabled).collect(Collectors.toList());
    }

    public Map<PostProcessStage, List<StagedPostProcessPipeline>> getPipelinesByStage() {
        EnumMap<PostProcessStage, List<StagedPostProcessPipeline>> map = initPipelinesByStage();
        for (StagedPostProcessPipeline p : STAGED_PIPELINES) {
            map.computeIfAbsent(p.getStage(), s -> new ArrayList<>()).add(p);
        }
        return map;
    }

    @SubscribeEvent
    private static void init(RegisterRenderingStuffEvent event) {
        quad = new Mesh(
                Vertices.FULLSCREEN_QUAD.vertices(),
                Vertices.FULLSCREEN_QUAD.indices(),
                VertexLayout.POS_TEX_2D,
                GL33.GL_TRIANGLES,
                true
        );
        // Scratch buffers are created lazily in ensureScratchBuffers().
    }

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
        // Reserved for future GUI-stage pipelines.
    }

    private static void runStage(PostProcessStage stage) {
        List<StagedPostProcessPipeline> pipelines = getEnabledPipelines(
                STAGED_PIPELINES, p -> p.getStage() == stage);
        if (!pipelines.isEmpty()) {
            runPipelineBatch(pipelines, stage);
        }
    }

    private static void runNonStagedPipelines() {
        List<PostProcessPipeline> pipelines = getEnabledPipelines(PIPELINES, p -> true);
        if (!pipelines.isEmpty()) {
            runPipelineBatch(pipelines, STAGE_NOT_STAGED);
        }
    }

    private static <T extends PostProcessPipeline> List<T> getEnabledPipelines(
            List<T> source,
            java.util.function.Predicate<T> extraFilter
    ) {
        return source.stream()
                .filter(PostProcessPipeline::isEnabled)
                .filter(extraFilter)
                .collect(Collectors.toList());
    }

    private static void runPipelineBatch(
            List<? extends PostProcessPipeline> pipelines,
            PostProcessStage stage
    ) {
        RenderSystem.assertOnRenderThread();

        RenderTarget mainTarget = Instances.getMainRenderTarget();
        ensureScratchBuffers(mainTarget);

        GlDispatch.pushDebugGroup(buildDebugGroupLabel(pipelines.size(), stage));
        GL_STACK.push();
        GlStateSnapshot saved = GlStateSnapshot.capture();
        try {
            setupGlobalState();

            var colorTexture = Instances.getGlColTexture();
            int mainColorTextureId = colorTexture.glId();
            int depthTextureId     = Instances.getGlDepthTexture().glId();
            var device             = Instances.getGlDevice();
            int mainFbo            = colorTexture.getFbo(device.directStateAccess(), null);

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
     * Executes every pass in the pipeline, routing inputs and outputs through the
     * correct framebuffers according to each pass's {@link PassTarget} declarations.
     *
     * <p><b>How target resolution works:</b></p>
     * <ul>
     *   <li>{@link PassTarget#MAIN} as <em>input</em>  → the current "live" main colour texture.</li>
     *   <li>{@link PassTarget#MAIN} as <em>output</em> → render into a scratch buffer, then blit
     *       that scratch buffer back into the main framebuffer so the result is visible.</li>
     *   <li>Any other target as <em>input</em>  → bind the colour texture of that scratch buffer.</li>
     *   <li>Any other target as <em>output</em> → bind that scratch buffer as the draw target.</li>
     * </ul>
     */
    private static void runPipeline(
            PostProcessPipeline pipeline,
            RenderTarget mainTarget,
            int mainFbo,
            int mainColorTextureId,
            int depthTextureId
    ) {
        List<PostProcessPipelinePass> passes = pipeline.getPasses();
        GlDispatch.pushDebugGroup("Pipeline: " + pipeline.getName());

        for (int i = 0; i < passes.size(); i++) {
            PostProcessPipelinePass pass    = passes.get(i);
            ShaderProgram           program = pipeline.getProgramForPass(i);

            // ── Resolve the actual render target for this pass's output ──────
            // When the declared output is MAIN we still render into a scratch buffer
            // and blit at the end; we never render directly into the main FBO.
            FrameBuffer drawBuffer = resolveOutputBuffer(pass.output(), mainTarget);

            // ── Resolve the texture this pass reads ──────────────────────────
            int inputColorTexId = resolveInputColorTexture(pass.input(), mainColorTextureId);

            renderPass(pipeline, program, i, pass, drawBuffer, inputColorTexId, depthTextureId);

            // ── If the pass wrote to a scratch buffer but declared MAIN as its
            //    output, blit the scratch result back into the real main FBO ──
            if (pass.output() == PassTarget.MAIN) {
                Instances.getFrameBufferManager().blit(drawBuffer, mainFbo, mainTarget);
                // The main colour texture has changed – refresh for subsequent passes.
                mainColorTextureId = Instances.getGlColTexture().glId();
            }
        }

        GlDispatch.popDebugGroup();
    }

    private static void renderPass(
            PostProcessPipeline pipeline,
            ShaderProgram program,
            int passIndex,
            PostProcessPipelinePass pass,
            FrameBuffer outputBuffer,
            int inputColorTexId,
            int depthTextureId
    ) {
        GlDispatch.pushDebugGroup("Pass " + passIndex + ": " + program.getId()
                + " [" + pass.input() + " -> " + pass.output() + "]");

        // Bind the output framebuffer and clear it.
        outputBuffer.bind();
        GlDispatch.glClear(GL43C.GL_COLOR_BUFFER_BIT);

        // Bind the input colour texture to unit 0.
        GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + COLOR_TEXTURE_UNIT);
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, inputColorTexId);

        // Always bind the main depth texture to unit 1.
        GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + DEPTH_TEXTURE_UNIT);
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, depthTextureId);

        // Activate shader, set uniforms, draw fullscreen quad.
        program.use();
        pipeline.setupDefaultUniforms(program);
        pipeline.setupUniforms(passIndex, pass);
        quad.draw();
        program.disable();

        outputBuffer.unbind();

        GlDispatch.popDebugGroup();
    }

    /**
     * Returns the {@link FrameBuffer} to render into for the given declared output target.
     *
     * <p>When the declared output is {@link PassTarget#MAIN} we cannot render directly into
     * Minecraft's framebuffer (it may be a native texture), so we redirect to
     * {@link PassTarget#PING} as a proxy and let the caller blit afterwards.</p>
     */
    private static FrameBuffer resolveOutputBuffer(PassTarget output, RenderTarget mainTarget) {
        if (output == PassTarget.MAIN) {
            // Use PING as the intermediate scratch when writing back to MAIN.
            return SCRATCH_BUFFERS.get(PassTarget.PING);
        }
        return SCRATCH_BUFFERS.get(output);
    }

    /**
     * Returns the OpenGL texture ID for the colour data this pass should sample.
     *
     * @param input             the pass's declared input target
     * @param mainColorTexId    the current main-target colour texture ID
     */
    private static int resolveInputColorTexture(PassTarget input, int mainColorTexId) {
        if (input == PassTarget.MAIN) {
            return mainColorTexId;
        }
        FrameBuffer buf = SCRATCH_BUFFERS.get(input);
        if (buf == null) {
            throw new IllegalStateException(
                    "No scratch buffer found for PassTarget." + input +
                            ". This is a bug – ensureScratchBuffers() should have created it.");
        }
        return buf.getColorTexture();
    }

    /**
     * Ensures every manager-owned {@link PassTarget} has a framebuffer that matches
     * the current main render target's dimensions.  Stale or missing buffers are
     * freed and re-created automatically.
     */
    private static void ensureScratchBuffers(RenderTarget mainTarget) {
        for (PassTarget target : PassTarget.values()) {
            if (!target.isManagerOwned()) continue;  // skip PassTarget.MAIN
            SCRATCH_BUFFERS.compute(target, (t, existing) ->
                    resizeOrCreate(existing, t.name().toLowerCase(), mainTarget));
        }
    }

    private static FrameBuffer resizeOrCreate(FrameBuffer buffer, String idSuffix, RenderTarget mainTarget) {
        if (buffer != null
                && buffer.width()  == mainTarget.width
                && buffer.height() == mainTarget.height) {
            return buffer;  // still valid
        }
        if (buffer != null) buffer.free();
        return new FrameBuffer(
                Commons.id("post_scratch_" + idSuffix),
                mainTarget.width,
                mainTarget.height,
                false,
                false
        );
    }

    private static void registerPipelinePrograms(PostProcessPipeline pipeline) {
        pipeline.getPrograms().forEach(Instances.getShaderManager()::register);
        pipeline.getPasses().stream()
                .flatMap(pass -> java.util.Arrays.stream(pass.shaders()))
                .forEach(Instances.getShaderManager()::register);
    }

    private static void setupGlobalState() {
        GlDispatch.glDisable(GL43C.GL_DEPTH_TEST);
        GlDispatch.glDisable(GL43C.GL_BLEND);
        GlDispatch.glDisable(GL43C.GL_CULL_FACE);
        GlDispatch.glDepthMask(false);
    }

    private static String buildDebugGroupLabel(int count, PostProcessStage stage) {
        String stageLabel = (stage == null) ? "UNSTAGED" : stage.name();
        return "Post-Process Pass (" + count + " pipelines, stage: " + stageLabel + ")";
    }

    private static EnumMap<PostProcessStage, List<StagedPostProcessPipeline>> initPipelinesByStage() {
        EnumMap<PostProcessStage, List<StagedPostProcessPipeline>> map =
                new EnumMap<>(PostProcessStage.class);
        for (PostProcessStage stage : PostProcessStage.values()) {
            map.put(stage, new ArrayList<>());
        }
        return map;
    }

    private record GlStateSnapshot(int readFbo, int drawFbo, int activeTexture, int[] textureBindings) {

        private static GlStateSnapshot capture() {
            int readFbo       = GlDispatch.glGetInteger(GL43C.GL_READ_FRAMEBUFFER_BINDING);
            int drawFbo       = GlDispatch.glGetInteger(GL43C.GL_DRAW_FRAMEBUFFER_BINDING);
            int activeTexture = GlDispatch.glGetInteger(GL43C.GL_ACTIVE_TEXTURE);
            int[] bindings    = new int[SAVED_TEXTURE_UNIT_COUNT];
            for (int i = 0; i < SAVED_TEXTURE_UNIT_COUNT; i++) {
                GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + i);
                bindings[i] = GlDispatch.glGetInteger(GL43C.GL_TEXTURE_BINDING_2D);
            }
            return new GlStateSnapshot(readFbo, drawFbo, activeTexture, bindings);
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