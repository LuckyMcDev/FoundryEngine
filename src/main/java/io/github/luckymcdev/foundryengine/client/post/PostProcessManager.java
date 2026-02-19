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
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.TargetRef;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.TemporaryTarget;
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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@EventBusSubscriber
public class PostProcessManager {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final OpenGlStack GL_STACK = Instances.getOpenGlStack();
    private static final int COLOR_TEXTURE_UNIT    = 0;
    private static final int DEPTH_TEXTURE_UNIT    = 1;
    private static final int SAVED_TEXTURE_UNIT_COUNT = 8;
    private static final PostProcessStage STAGE_NOT_STAGED = null;

    // -------------------------------------------------------------------------
    // Pipeline registries
    // -------------------------------------------------------------------------

    private static final List<PostProcessPipeline>       PIPELINES        = new ArrayList<>();
    private static final List<StagedPostProcessPipeline> STAGED_PIPELINES = new ArrayList<>();

    /**
     * Per-pipeline scratch buffers, keyed by pipeline identity then by target name.
     *
     * <p>Each pipeline gets its own map so that target names are scoped to the pipeline
     * that declared them (just as Minecraft's {@code PostChain} targets are local to
     * each chain file).  The special sentinel name {@code "main"} is never stored here.</p>
     */
    private static final Map<PostProcessPipeline, Map<String, FrameBuffer>> PIPELINE_BUFFERS
            = new IdentityHashMap<>();

    /**
     * A single shared "blit proxy" buffer used when a pass declares
     * {@link TargetRef#MAIN} as its output.  We never render directly into
     * Minecraft's main FBO, so we render here then blit.
     *
     * <p>This buffer is resized to match the main target each frame, exactly once,
     * regardless of how many pipelines need it.</p>
     */
    private static FrameBuffer blitProxy;

    private static Mesh quad;

    // -------------------------------------------------------------------------
    // Pipeline management
    // -------------------------------------------------------------------------

    public void addPipeline(PostProcessPipeline pipeline) {
        PIPELINES.add(pipeline);
        registerPipelinePrograms(pipeline);
    }

    public void enablePipeline(PostProcessPipeline pipeline) {
        if (PIPELINES.contains(pipeline)) pipeline.enable();
    }

    public void disablePipeline(PostProcessPipeline pipeline) {
        pipeline.disable();
    }

    public void addPipeline(StagedPostProcessPipeline pipeline) {
        STAGED_PIPELINES.add(pipeline);
        registerPipelinePrograms(pipeline);
    }

    public void enablePipeline(StagedPostProcessPipeline pipeline) {
        if (STAGED_PIPELINES.contains(pipeline) && !pipeline.isEnabled()) pipeline.enable();
    }

    public void disablePipeline(StagedPostProcessPipeline pipeline) {
        pipeline.disable();
    }

    public void changePipelineStage(StagedPostProcessPipeline pipeline, PostProcessStage newStage) {
        if (STAGED_PIPELINES.contains(pipeline)) pipeline.setStage(newStage);
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

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @SubscribeEvent
    private static void init(RegisterRenderingStuffEvent event) {
        quad = new Mesh(
                Vertices.FULLSCREEN_QUAD.vertices(),
                Vertices.FULLSCREEN_QUAD.indices(),
                VertexLayout.POS_TEX_2D,
                GL33.GL_TRIANGLES,
                true
        );
        // All framebuffers are created lazily in ensureFrameBuffers().
    }

    // -------------------------------------------------------------------------
    // Stage event handlers
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Run helpers
    // -------------------------------------------------------------------------

    private static void runStage(PostProcessStage stage) {
        List<StagedPostProcessPipeline> pipelines =
                getEnabledPipelines(STAGED_PIPELINES, p -> p.getStage() == stage);
        if (!pipelines.isEmpty()) runPipelineBatch(pipelines, stage);
    }

    private static void runNonStagedPipelines() {
        List<PostProcessPipeline> pipelines = getEnabledPipelines(PIPELINES, p -> true);
        if (!pipelines.isEmpty()) runPipelineBatch(pipelines, STAGE_NOT_STAGED);
    }

    private static <T extends PostProcessPipeline> List<T> getEnabledPipelines(
            List<T> source, Predicate<T> extraFilter) {
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
        ensureFrameBuffers(pipelines, mainTarget);

        GlDispatch.pushDebugGroup(buildDebugGroupLabel(pipelines.size(), stage));
        GL_STACK.push();
        GlStateSnapshot saved = GlStateSnapshot.capture();
        try {
            setupGlobalState();

            var colorTexture       = Instances.getGlColTexture();
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

    // -------------------------------------------------------------------------
    // Per-pipeline execution
    // -------------------------------------------------------------------------

    /**
     * Executes every pass in the pipeline, routing inputs and outputs through
     * the framebuffers that match each pass's {@link TargetRef} declarations.
     */
    private static void runPipeline(
            PostProcessPipeline pipeline,
            RenderTarget mainTarget,
            int mainFbo,
            int mainColorTextureId,
            int depthTextureId
    ) {
        List<PostProcessPipelinePass> passes     = pipeline.getPasses();
        Map<String, FrameBuffer>      localBuffers = PIPELINE_BUFFERS.get(pipeline);

        GlDispatch.pushDebugGroup("Pipeline: " + pipeline.getName());

        for (int i = 0; i < passes.size(); i++) {
            PostProcessPipelinePass pass    = passes.get(i);
            ShaderProgram           program = pipeline.getProgramForPass(i);

            // ── Resolve draw target ───────────────────────────────────────────
            // Writing to MAIN → render into the shared blitProxy, blit afterwards.
            FrameBuffer drawBuffer = resolveOutputBuffer(pass.output(), localBuffers);

            // ── Resolve input colour texture ──────────────────────────────────
            int inputColorTexId = resolveInputColorTexture(pass.input(), mainColorTextureId, localBuffers);

            renderPass(pipeline, program, i, pass, drawBuffer, inputColorTexId, depthTextureId);

            // ── Blit proxy → main FBO when the pass declared MAIN as output ───
            if (pass.output().isMain()) {
                Instances.getFrameBufferManager().blit(drawBuffer, mainFbo, mainTarget);
                // Refresh the live colour texture ID for subsequent passes.
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

        outputBuffer.bind();
        GlDispatch.glClear(GL43C.GL_COLOR_BUFFER_BIT);

        GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + COLOR_TEXTURE_UNIT);
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, inputColorTexId);

        GlDispatch.glActiveTexture(GL43C.GL_TEXTURE0 + DEPTH_TEXTURE_UNIT);
        GlDispatch.glBindTexture(GL43C.GL_TEXTURE_2D, depthTextureId);

        program.use();
        pipeline.setupDefaultUniforms(program);
        pipeline.setupUniforms(passIndex, pass);
        quad.draw();
        program.disable();

        outputBuffer.unbind();

        GlDispatch.popDebugGroup();
    }

    // -------------------------------------------------------------------------
    // Target resolution
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link FrameBuffer} to draw into for the given output target.
     *
     * <ul>
     *   <li>{@link TargetRef#MAIN} → shared {@link #blitProxy} (blit back to main FBO afterwards)</li>
     *   <li>Named temp → the pipeline-local buffer for that name</li>
     * </ul>
     */
    private static FrameBuffer resolveOutputBuffer(TargetRef output, Map<String, FrameBuffer> localBuffers) {
        if (output.isMain()) {
            return blitProxy;
        }
        FrameBuffer buf = localBuffers.get(output.getName());
        if (buf == null) {
            throw new IllegalStateException(
                    "No temporary target \"" + output.getName() + "\" found. " +
                            "Did you call addTarget(\"" + output.getName() + "\") in your pipeline constructor?");
        }
        return buf;
    }

    /**
     * Returns the OpenGL colour texture ID that the given input target should sample.
     *
     * <ul>
     *   <li>{@link TargetRef#MAIN} → the current live main colour texture ID</li>
     *   <li>Named temp → the colour texture of the pipeline-local buffer for that name</li>
     * </ul>
     */
    private static int resolveInputColorTexture(
            TargetRef input,
            int mainColorTexId,
            Map<String, FrameBuffer> localBuffers
    ) {
        if (input.isMain()) {
            return mainColorTexId;
        }
        FrameBuffer buf = localBuffers.get(input.getName());
        if (buf == null) {
            throw new IllegalStateException(
                    "No temporary target \"" + input.getName() + "\" found. " +
                            "Did you call addTarget(\"" + input.getName() + "\") in your pipeline constructor?");
        }
        return buf.getColorTexture();
    }

    // -------------------------------------------------------------------------
    // Framebuffer lifecycle
    // -------------------------------------------------------------------------

    /**
     * Ensures every pipeline has correctly-sized framebuffers for all its
     * {@link TemporaryTarget}s, and that the shared {@link #blitProxy} is current.
     * Stale or missing buffers are freed and re-created automatically.
     */
    private static void ensureFrameBuffers(
            List<? extends PostProcessPipeline> pipelines,
            RenderTarget mainTarget
    ) {
        // Shared blit proxy (used for any MAIN output across all pipelines).
        blitProxy = resizeOrCreate(blitProxy, "blit_proxy", mainTarget);

        for (PostProcessPipeline pipeline : pipelines) {
            Map<String, FrameBuffer> localBuffers =
                    PIPELINE_BUFFERS.computeIfAbsent(pipeline, p -> new LinkedHashMap<>());

            for (TemporaryTarget target : pipeline.getTargets().values()) {
                localBuffers.compute(target.name(), (name, existing) ->
                        resizeOrCreate(existing, pipeline.getName().getPath() + "_" + name, mainTarget));
            }
        }
    }

    private static FrameBuffer resizeOrCreate(FrameBuffer buffer, String idSuffix, RenderTarget mainTarget) {
        if (buffer != null
                && buffer.width()  == mainTarget.width
                && buffer.height() == mainTarget.height) {
            return buffer;   // still valid, nothing to do
        }
        if (buffer != null) buffer.free();
        return new FrameBuffer(
                Commons.id("post_" + idSuffix),
                mainTarget.width,
                mainTarget.height,
                false,
                false
        );
    }

    // -------------------------------------------------------------------------
    // Misc helpers
    // -------------------------------------------------------------------------

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