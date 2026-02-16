package io.github.luckymcdev.foundryengine.client.post.pipeline;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniforms;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import jdk.jfr.Enabled;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PostProcessPipeline {
    private final List<ShaderProgram> passes = new ArrayList<>();
    private final Identifier name;
    private boolean enabled;

    public PostProcessPipeline(Identifier name, PostProcessPipelinePass... passes) {
        this.name = name;
        this.enabled = false;
        for (PostProcessPipelinePass pass : passes) {
            ShaderProgram program = new ShaderProgram(pass.name(), pass.shaders());
            try {
                program.link();
                this.passes.add(program);
            } catch (ShaderException e) {
                throw new RuntimeException("Failed to link pass: " + pass.name(), e);
            }
        }
    }

    public List<ShaderProgram> getPasses() {
        return passes;
    }

    public final void setupDefaultUniforms(ShaderProgram program) {
        // Set uniforms on the specified program (should be currently active)
        program.setUniforms(Uniforms.getCollection());
        program.setUniform(new Uniform<>("screenTexture", 0));
        program.setUniform(new Uniform<>("depthTexture", 1));
    }

    public Identifier getName() {
        return name;
    }

    // Pass index allows you to set uniforms for specific steps (e.g., Blur direction)
    public void setupUniforms(int passIndex) {
    }

    public final void enable() {
        this.enabled = true;
    }

    public final void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    // Helper for simple one-pass pipelines
    public ShaderProgram getProgram() {
        return passes.getFirst();
    }


}