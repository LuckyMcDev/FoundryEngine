package io.github.luckymcdev.foundryengine.client.post.pipeline;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniforms;
import io.github.luckymcdev.foundryengine.client.post.pipeline.param.PipelineParam;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * A named collection of {@link PostProcessPipelinePass passes} that together
 * produce a post-processing effect.
 *
 * <h3>Declaring editable parameters</h3>
 * Subclasses call {@link #addParam} in their constructor to register
 * {@link PipelineParam} instances. These are:
 * <ul>
 *   <li>Automatically uploaded as uniforms to every pass each frame.</li>
 *   <li>Exposed to the {@code PostProcessPanel} so they can be tweaked live
 *       without any extra panel code.</li>
 * </ul>
 *
 * <pre>{@code
 * public class MyPipeline extends PostProcessPipeline {
 *     private final PipelineParam<Float> brightness =
 *         addParam(PipelineParam.floatParam("brightness", 1.0f, 0.0f, 3.0f));
 *
 *     public MyPipeline(Identifier name, PostProcessPipelinePass... passes) {
 *         super(name, passes);
 *     }
 *     // No setupUniforms override needed – params are applied automatically.
 * }
 * }</pre>
 */
public class PostProcessPipeline {

    /** Parallel lists: passes[i] ↔ programs[i] */
    private final List<PostProcessPipelinePass> passes   = new ArrayList<>();
    private final List<ShaderProgram>           programs = new ArrayList<>();

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
     * depend on runtime state (e.g. window resolution, camera data) not
     * expressible as a static {@link PipelineParam}.
     *
     * <p>Most pipelines don't need to override this at all — declare
     * {@link PipelineParam}s instead, and they'll be applied automatically.</p>
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