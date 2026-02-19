package io.github.luckymcdev.foundryengine.client.post.pipeline;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniforms;
import io.github.luckymcdev.foundryengine.client.post.pipeline.param.PipelineParam;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.TemporaryTarget;
import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * A named collection of {@link PostProcessPipelinePass passes} that together produce a
 * post-processing effect.
 *
 * <h3>Declaring temporary framebuffers</h3>
 * Call {@link #addTarget} in the subclass constructor to register {@link TemporaryTarget}s.
 * The {@code PostProcessManager} allocates one {@code FrameBuffer} per unique name per
 * pipeline, mirroring Minecraft's {@code PostChain} {@code <target>} entries.
 *
 * <pre>{@code
 * public class BloomPipeline extends PostProcessPipeline {
 *     public BloomPipeline(Identifier name, PostProcessPipelinePass... passes) {
 *         super(name, passes);
 *         addTarget(TemporaryTarget.named("blur_h"));
 *         addTarget(TemporaryTarget.named("blur_v"));
 *     }
 * }
 * }</pre>
 *
 * <h3>Declaring editable parameters</h3>
 * Call {@link #addParam} in the subclass constructor to register {@link PipelineParam}s.
 * They are automatically uploaded as uniforms every frame and exposed in the editor panel.
 *
 * <pre>{@code
 * private final PipelineParam<Float> brightness =
 *     addParam(PipelineParam.floatParam("brightness", 1.0f, 0.0f, 3.0f));
 * }</pre>
 */
public class PostProcessPipeline {

    /** Parallel lists: passes[i] ↔ programs[i] */
    private final List<PostProcessPipelinePass> passes   = new ArrayList<>();
    private final List<ShaderProgram>           programs = new ArrayList<>();

    /**
     * Named temporary framebuffer slots required by this pipeline.
     * Insertion order is preserved; the manager iterates these to allocate buffers.
     */
    private final LinkedHashMap<String, TemporaryTarget> targets = new LinkedHashMap<>();

    /**
     * Ordered map of uniform name → param.
     * LinkedHashMap preserves insertion order for a predictable panel layout.
     */
    private final Map<String, PipelineParam<?>> params = new LinkedHashMap<>();

    private final Identifier name;
    private boolean enabled;

    public PostProcessPipeline(Identifier name, PostProcessPipelinePass... passes) {
        this.name    = name;
        this.enabled = false;
        for (PostProcessPipelinePass pass : passes) {
            ShaderProgram program = new ShaderProgram(pass.name(), pass.shaders());
            try {
                program.link();
                this.passes.add(pass);
                this.programs.add(program);
            } catch (ShaderException e) {
                throw new RuntimeException("Failed to link pass: " + pass.name(), e);
            }
        }
    }

    // =========================================================================
    // Temporary target registration
    // =========================================================================

    /**
     * Registers a {@link TemporaryTarget} for this pipeline.
     * The manager will allocate (and auto-resize) a {@code FrameBuffer} for each
     * unique target name before running the pipeline.
     *
     * <p>Call from the subclass constructor, <em>after</em> {@code super(...)}.</p>
     */
    protected TemporaryTarget addTarget(TemporaryTarget target) {
        targets.put(target.name(), target);
        return target;
    }

    /**
     * Convenience overload – creates and registers a target in one call.
     * <pre>{@code addTarget("blur_ping"); }</pre>
     */
    protected TemporaryTarget addTarget(String name) {
        return addTarget(TemporaryTarget.named(name));
    }

    /** Returns an unmodifiable view of this pipeline's declared temporary targets. */
    public Map<String, TemporaryTarget> getTargets() {
        return Collections.unmodifiableMap(targets);
    }

    // =========================================================================
    // Param registration
    // =========================================================================

    /**
     * Registers a {@link PipelineParam} and returns it so the subclass can keep
     * a typed reference for programmatic access.
     *
     * <p>Call this in the subclass constructor, <em>after</em> {@code super(...)}.</p>
     */
    protected <T, P extends PipelineParam<T>> P addParam(P param) {
        params.put(param.getUniformName(), param);
        return param;
    }

    /** Returns all registered params in declaration order (used by the panel). */
    public Map<String, PipelineParam<?>> getParams() {
        return Collections.unmodifiableMap(params);
    }

    // =========================================================================
    // Pass / program accessors
    // =========================================================================

    public List<PostProcessPipelinePass> getPasses() {
        return Collections.unmodifiableList(passes);
    }

    public ShaderProgram getProgramForPass(int index) {
        return programs.get(index);
    }

    /** Convenience for single-pass pipelines. */
    public ShaderProgram getProgram() {
        return programs.getFirst();
    }

    public List<ShaderProgram> getPrograms() {
        return programs;
    }

    // =========================================================================
    // Uniform helpers (called by PostProcessManager)
    // =========================================================================

    /**
     * Sets the uniforms every pass needs: global engine uniforms, standard texture
     * samplers on units 0/1/2, and all registered {@link PipelineParam}s.
     *
     * <p>Called by the manager before {@link #setupUniforms(int, PostProcessPipelinePass)}.</p>
     */
    public final void setupDefaultUniforms(ShaderProgram program) {
        program.setUniforms(Uniforms.getCollection());
        program.setUniform(new Uniform<>("screenTexture",   0));
        program.setUniform(new Uniform<>("depthTexture",    1));
        program.setUniform(new Uniform<>("originalTexture", 2));

        // Auto-apply every registered param to this program.
        for (PipelineParam<?> param : params.values()) {
            param.applyToProgram(program);
        }
    }

    /**
     * Override to supply per-pass uniforms that vary between passes, or that
     * depend on runtime state not expressible as a static {@link PipelineParam}.
     *
     * <p>Most pipelines don't need to override this – declare {@link PipelineParam}s
     * instead, and they'll be applied automatically.</p>
     */
    public void setupUniforms(int passIndex, PostProcessPipelinePass pass) {
        // default: params already applied in setupDefaultUniforms
    }

    // =========================================================================
    // Enable / disable
    // =========================================================================

    public final void enable()   { this.enabled = true;  }
    public final void disable()  { this.enabled = false; }
    public boolean isEnabled()   { return this.enabled;  }

    public Identifier getName()  { return name; }
}