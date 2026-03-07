package io.github.luckymcdev.foundryengine.client.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.event.RegisterRenderingStuffEvent;
import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlStack;
import io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBuffer;
import io.github.luckymcdev.foundryengine.client.opengl.mesh.DrawMode;
import io.github.luckymcdev.foundryengine.client.opengl.mesh.Mesh;
import io.github.luckymcdev.foundryengine.client.opengl.mesh.VertexLayout;
import io.github.luckymcdev.foundryengine.client.opengl.mesh.Vertices;
import io.github.luckymcdev.foundryengine.client.opengl.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.TargetRef;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.common.Common;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL43C;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Manager for all {@link PostProcessPipeline} and {@link StagedPostProcessPipeline}
 * <p>
 * Register and Enable them from here.
 * Registering should be done via {@link RegisterPostPipelineEvent}
 */
@EventBusSubscriber(Dist.CLIENT)
public class PostProcessManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final OpenGlStack GL_STACK = Client.getOpenGlStack();
    private static final int COLOR_TEXTURE_UNIT = 0;
    private static final int DEPTH_TEXTURE_UNIT = 1;
    private static final PostProcessStage STAGE_NOT_STAGED = null;
    private static final List<PostProcessPipeline> PIPELINES = new ArrayList<>();
    private static final Map<PostProcessPipeline, Map<String, FrameBuffer>> PIPELINE_BUFFERS = new IdentityHashMap<>();
    private static FrameBuffer blitProxy;
    private static Mesh quad;

    public PostProcessManager() {
    }
    /**
     * Initializes the GpuMesh quad.
     * @param event
     */
    @SubscribeEvent
    private static void init(RegisterRenderingStuffEvent event) {
        quad = new Mesh(Vertices.FULLSCREEN_QUAD, VertexLayout.POS_TEX_2D, DrawMode.TRIANGLES);
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
        runStage(PostProcessStage.FINAL);
    }

    @SubscribeEvent
    public static void onAfterRender(RenderGuiEvent.Post event) {
        // Reserved for future GUI-stage pipelines.
    }

    /**
     * Runs all Pipelines for this {@link PostProcessStage}
     * @param stage the Stage to Run.
     */
    private static void runStage(PostProcessStage stage) {
        List<PostProcessPipeline> pipelines =
                getEnabledPipelines(PIPELINES, p -> p.getStage() == stage);
        if (!pipelines.isEmpty()) runPipelineBatch(pipelines, stage);
    }

    /**
     * Returns all Enabled Pipelines from a list of pipelines and an extra filter.
     *
     * @param source      the source list to check.
     * @param extraFilter extra filter to check for.
     * @param <T>         Pipeline.
     * @return the filtered and collected List.
     */
    private static <T extends PostProcessPipeline> List<T> getEnabledPipelines(List<T> source, Predicate<T> extraFilter) {
        return source.stream()
                .filter(PostProcessPipeline::isEnabled)
                .filter(extraFilter)
                .collect(Collectors.toList());
    }

    /**
     * Runs a Batch of Pipelines for a Post Processing Stage.
     * @param pipelines the List of Pipelines to run.
     * @param stage the stage we are at.
     */
    private static void runPipelineBatch(List<? extends PostProcessPipeline> pipelines, PostProcessStage stage) {
        RenderSystem.assertOnRenderThread();

        RenderTarget mainTarget = Client.getMainRenderTarget();
        ensureFrameBuffers(pipelines, mainTarget);

        GlDispatch.pushDebugGroup(buildDebugGroupLabel(pipelines.size(), stage));
        GL_STACK.push();
        try {
            setupGlobalState();

            var colorTexture = Client.getGlColTexture();
            int mainColorTextureId = colorTexture.glId();
            int depthTextureId = Client.getGlDepthTexture().glId();
            var device = Client.getGlDevice();
            int mainFbo = colorTexture.getFbo(device.directStateAccess(), null);

            for (PostProcessPipeline pipeline : pipelines) {
                runPipeline(pipeline, mainTarget, mainFbo, mainColorTextureId, depthTextureId);
            }
        } finally {
            GlDispatch.popDebugGroup();
            GL_STACK.pop();
        }
    }

    /**
     * Executes every pass in the pipeline, routing inputs and outputs through
     * the framebuffers that match each pass's {@link TargetRef} declarations.
     */
    private static void runPipeline(PostProcessPipeline pipeline, RenderTarget mainTarget, int mainFbo, int mainColorTextureId, int depthTextureId) {
        List<PostProcessPipelinePass> passes = pipeline.getPasses();
        Map<String, FrameBuffer> localBuffers = PIPELINE_BUFFERS.get(pipeline);

        GlDispatch.pushDebugGroup("Pipeline: " + pipeline.getName());

        for (int i = 0; i < passes.size(); i++) {
            PostProcessPipelinePass pass = passes.get(i);
            ShaderProgram program = pipeline.getProgramForPass(i);

            FrameBuffer drawBuffer = resolveOutputBuffer(pass.output(), localBuffers);

            int inputColorTexId = resolveInputColorTexture(pass.input(), mainColorTextureId, localBuffers);

            renderPass(pipeline, program, i, pass, drawBuffer, inputColorTexId, depthTextureId);

            if (pass.output().isMain()) {
                Client.getFrameBufferManager().blit(drawBuffer, mainFbo, mainTarget);
                mainColorTextureId = Client.getGlColTexture().glId();
            }
        }

        GlDispatch.popDebugGroup();
    }

    /**
     * Processes a Single Pass.
     */
    private static void renderPass(PostProcessPipeline pipeline, ShaderProgram program, int passIndex, PostProcessPipelinePass pass, FrameBuffer outputBuffer, int inputColorTexId, int depthTextureId) {
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
        pipeline.setupUniforms(pass, program);
        quad.draw();
        program.disable();

        outputBuffer.unbind();

        GlDispatch.popDebugGroup();
    }

    /**
     * Returns the {@link FrameBuffer} to draw into for the given output target.
     *
     * <ul>
     *   <li>{@link TargetRef#MAIN} -> shared {@link #blitProxy} (blit back to main FBO afterward)</li>
     *   <li>Named temp -> the pipeline-local buffer for that name</li>
     * </ul>
     */
    private static FrameBuffer resolveOutputBuffer(TargetRef output, Map<String, FrameBuffer> localBuffers) {
        if (output.isMain()) {
            return blitProxy;
        }
        FrameBuffer buf = localBuffers.get(output.name());
        if (buf == null) {
            throw new IllegalStateException(
                    "No temporary target \"" + output.name() + "\" found. " +
                            "Did you call addTarget(\"" + output.name() + "\") in your pipeline constructor?");
        }
        return buf;
    }

    /**
     * Returns the OpenGL color texture ID that the given input target should sample.
     *
     * <ul>
     *   <li>{@link TargetRef#MAIN} -> the current live main color texture ID</li>
     *   <li>Named temp -> the color texture of the pipeline-local buffer for that name</li>
     * </ul>
     */
    private static int resolveInputColorTexture(TargetRef input, int mainColorTexId, Map<String, FrameBuffer> localBuffers) {
        if (input.isMain()) {
            return mainColorTexId;
        }
        FrameBuffer buf = localBuffers.get(input.name());
        if (buf == null) {
            throw new IllegalStateException(
                    "No temporary target \"" + input.name() + "\" found. " +
                            "Did you call addTarget(\"" + input.name() + "\") in your pipeline constructor?");
        }
        return buf.getColorTexture();
    }

    /**
     * Ensures every pipeline has correctly-sized framebuffers for all its
     * {@link TargetRef}s, and that the shared {@link #blitProxy} is current.
     * Stale or missing buffers are freed and re-created automatically.
     */
    private static void ensureFrameBuffers(List<? extends PostProcessPipeline> pipelines, RenderTarget mainTarget) {
        // Shared blit proxy (used for any MAIN output across all pipelines).
        blitProxy = resizeOrCreate(blitProxy, "blit_proxy", mainTarget);

        for (PostProcessPipeline pipeline : pipelines) {
            Map<String, FrameBuffer> localBuffers =
                    PIPELINE_BUFFERS.computeIfAbsent(pipeline, p -> new LinkedHashMap<>());

            for (TargetRef target : pipeline.getTargets().values()) {
                localBuffers.compute(target.name(), (name, existing) ->
                        resizeOrCreate(existing, pipeline.getName().getPath() + "_" + name, mainTarget));
            }
        }
    }

    private static FrameBuffer resizeOrCreate(FrameBuffer buffer, String idSuffix, RenderTarget mainTarget) {
        if (buffer != null
                && buffer.width() == mainTarget.width
                && buffer.height() == mainTarget.height) {
            return buffer;   // still valid, nothing to do
        }
        if (buffer != null) buffer.free();
        return new FrameBuffer(
                Common.id("post_" + idSuffix),
                mainTarget.width,
                mainTarget.height,
                false,
                false
        );
    }

    private static void registerPipelinePrograms(PostProcessPipeline pipeline) {
        pipeline.getPrograms().forEach(Client.getShaderManager()::register);
        pipeline.getPasses().stream()
                .flatMap(pass -> java.util.Arrays.stream(pass.shaders()))
                .forEach(Client.getShaderManager()::register);
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

    private static EnumMap<PostProcessStage, List<PostProcessPipeline>> initPipelinesByStage() {
        EnumMap<PostProcessStage, List<PostProcessPipeline>> map =
                new EnumMap<>(PostProcessStage.class);
        for (PostProcessStage stage : PostProcessStage.values()) {
            map.put(stage, new ArrayList<>());
        }
        return map;
    }

    public void enablePipeline(PostProcessPipeline pipeline) {
        if (PIPELINES.contains(pipeline)) pipeline.enable();
    }

    public void disablePipeline(PostProcessPipeline pipeline) {
        pipeline.disable();
    }

    public void changePipelineStage(PostProcessPipeline pipeline, PostProcessStage newStage) {
        if (PIPELINES.contains(pipeline)) pipeline.setStage(newStage);
    }

    public void addPipeline(PostProcessPipeline pipeline) {
        // Check if a pipeline with this ID already exists
        boolean exists = PIPELINES.stream()
                .anyMatch(p -> p.getName().equals(pipeline.getName()));

        if (!exists) {
            PIPELINES.add(pipeline);
            registerPipelinePrograms(pipeline);
        } else {
            LOGGER.debug("Staged pipeline '{}' already registered, skipping duplicate", pipeline.getName());
        }
    }

    public List<PostProcessPipeline> getPipelines() {
        return PIPELINES;
    }

    public List<PostProcessPipeline> getEnabledPipelines() {
        return PIPELINES.stream().filter(PostProcessPipeline::isEnabled).collect(Collectors.toList());
    }

    public Map<PostProcessStage, List<PostProcessPipeline>> getPipelinesByStage() {
        EnumMap<PostProcessStage, List<PostProcessPipeline>> map = initPipelinesByStage();
        for (PostProcessPipeline p : PIPELINES) {
            map.computeIfAbsent(p.getStage(), s -> new ArrayList<>()).add(p);
        }
        return map;
    }
}