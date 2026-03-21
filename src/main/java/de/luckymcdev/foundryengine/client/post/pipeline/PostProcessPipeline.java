package de.luckymcdev.foundryengine.client.post.pipeline;

import de.luckymcdev.foundryengine.client.opengl.exeption.ShaderException;
import de.luckymcdev.foundryengine.client.opengl.program.ShaderProgram;
import de.luckymcdev.foundryengine.client.opengl.uniform.Uniform;
import de.luckymcdev.foundryengine.client.opengl.uniform.Uniforms;
import de.luckymcdev.foundryengine.client.post.pipeline.param.PipelineParam;
import de.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import de.luckymcdev.foundryengine.client.post.pipeline.pass.TargetRef;
import de.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import de.luckymcdev.foundryengine.common.exeptions.EngineException;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * A post Process pipeline for minecraft. look at {@link de.luckymcdev.foundryengine.client.post.pipeline.builtin}
 * for some examples.
 * By default, disabled. Call {@link PostProcessPipeline#enable()} in the constructor of your Pipeline
 * to enable instantly.
 */
@ApiStatus.Experimental
public abstract class PostProcessPipeline {
    private final List<ShaderProgram> programs = new ArrayList<>();
    private final LinkedHashMap<String, TargetRef> targets = new LinkedHashMap<>();
    private final Map<String, PipelineParam<?>> params = new LinkedHashMap<>();
    private boolean enabled;
    private PostProcessStage stage;

    /**
     * Create a new PostProcessPipeline
     */
    protected PostProcessPipeline() {
        this.enabled = false;
        this.stage = getInitialStage();
        List<PostProcessPipelinePass> declaredPasses = getPasses();
        for (PostProcessPipelinePass pass : declaredPasses) {
            ShaderProgram program = new ShaderProgram(pass.name(), pass.shaders());
            try {
                program.link();
                this.programs.add(program);
            } catch (ShaderException e) {
                throw new EngineException("Failed to link pass: " + pass.name(), e);
            }
        }
    }

    public abstract Identifier getName();

    public abstract PostProcessStage getInitialStage();

    public abstract List<PostProcessPipelinePass> getPasses();

    /**
     * Add a new {@link TargetRef} to this Pipeline.
     * @param target the Target to add.
     * @return the Target which got Added.
     */
    protected TargetRef addTarget(TargetRef target) {
        targets.put(target.name(), target);
        return target;
    }

    /**
     * Same as {@link #addTarget(TargetRef)} except with a String param instead of a Full {@link TargetRef}
     * @param name the name of the Target to add.
     * @return the Target which got added.
     */
    protected TargetRef addTarget(String name) {
        return addTarget(TargetRef.named(name));
    }

    /**
     * Returns a View of the Targets available in this Pipeline.
     * @return the view of the Targets available.
     */
    public Map<String, TargetRef> getTargets() {
        return Collections.unmodifiableMap(targets);
    }

    /**
     * Registers a {@link PipelineParam} and returns it so the subclass can keep
     * a typed reference for programmatic access.
     *  <br>
     * Call this in the subclass constructor, after {@code super(...)}.
     * @param <T> the Type T
     * @param <P> the Param P
     * @param param the param
     * @return the param.
     */
    protected <T, P extends PipelineParam<T>> P addParam(P param) {
        params.put(param.getUniformName(), param);
        return param;
    }

    /**
     * Returns all registered params in declaration order (used by the panel).
     * @return a Map of all params.
     */
    public Map<String, PipelineParam<?>> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public ShaderProgram getProgramForPass(int index) {
        return programs.get(index);
    }

    /**
     * Convenience for single-pass pipelines.
     * @return the first {@link ShaderProgram} out of the List.
     */
    public ShaderProgram getProgram() {
        return programs.getFirst();
    }

    public List<ShaderProgram> getPrograms() {
        return programs;
    }

    /**
     * Sets a collection of default Uniforms.
     * Includes:
     *  - screenTexture
     *  - depthTexture
     * <br>
     * You don't need to call this, it's done automatically.
     * @param program the Program for which to set these Uniforms.
     */
    public final void setupDefaultUniforms(ShaderProgram program) {
        program.setUniforms(Uniforms.getCollection());
        program.setUniform(new Uniform<>("screenTexture", () -> 0));
        program.setUniform(new Uniform<>("depthTexture", () -> 1));
        program.setUniform(new Uniform<>("originalTexture", () -> 2));

        for (PipelineParam<?> param : params.values()) {
            param.applyToProgram(program);
        }
    }

    /**
     * Override this to set up Custom Uniforms for your PostProcessPipeline.
     * Per Pass.
     * <br>
     * To check for a specific pass, you can use an if statement.
     *
     * @param pass    the Pass for which to set the Uniforms
     * @param program the Program with which you can set them {@link ShaderProgram#setUniform(Uniform)}
     */
    public void setupUniforms(PostProcessPipelinePass pass, ShaderProgram program) {
    }

    /**
     * Enables this Pipeline.
     */
    public final void enable() {
        this.enabled = true;
    }

    /**
     * Disables this Pipeline
     */
    public final void disable() {
        this.enabled = false;
    }

    /**
     * Weather or not this Pipeline is enabled.
     *
     * @return enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Gets the stage at which this pipeline renders.
     *
     * @return The {@link PostProcessStage} of this pipeline.
     */
    public PostProcessStage getStage() {
        return stage;
    }

    /**
     * Sets the stage at which this pipeline renders.
     *
     * @param stage The new {@link PostProcessStage} for this pipeline.
     */
    public void setStage(PostProcessStage stage) {
        this.stage = stage;
    }
}