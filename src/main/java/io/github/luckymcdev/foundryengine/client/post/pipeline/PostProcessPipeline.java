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

public class PostProcessPipeline {
    private final List<PostProcessPipelinePass> passes = new ArrayList<>();
    private final List<ShaderProgram> programs = new ArrayList<>();
    private final LinkedHashMap<String, TemporaryTarget> targets = new LinkedHashMap<>();
    private final Map<String, PipelineParam<?>> params = new LinkedHashMap<>();

    private final Identifier name;
    private boolean enabled;

    /**
     * Create a new PostProcessPipeline
     *
     * @param name   The unique identifier for this Post Process Pipeline.
     * @param passes n Amount of Passes for this PostProcessPipeline
     */
    public PostProcessPipeline(Identifier name, PostProcessPipelinePass... passes) {
        this.name = name;
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

    /**
     * Add a new {@link TemporaryTarget} to this Pipeline.
     * @param target the Target to add.
     * @return the Target which got Added.
     */
    protected TemporaryTarget addTarget(TemporaryTarget target) {
        targets.put(target.name(), target);
        return target;
    }

    /**
     * Same as {@link #addTarget(TemporaryTarget)} except with a String param instead of a Full {@link TemporaryTarget}
     * @param name the name of the Target to add.
     * @return the Target which got added.
     */
    protected TemporaryTarget addTarget(String name) {
        return addTarget(TemporaryTarget.named(name));
    }

    /**
     * Returns a View of the Targets available in this Pipeline.
     * @return the view of the Targets available.
     */
    public Map<String, TemporaryTarget> getTargets() {
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

    public List<PostProcessPipelinePass> getPasses() {
        return Collections.unmodifiableList(passes);
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
     * The Identifier for this Pipeline.
     *
     * @return the Identifier for this Pipeline.
     */
    public Identifier getName() {
        return name;
    }
}