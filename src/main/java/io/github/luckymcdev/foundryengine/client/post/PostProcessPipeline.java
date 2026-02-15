package io.github.luckymcdev.foundryengine.client.post;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniforms;

public class PostProcessPipeline {
    private final ShaderProgram program;

    public PostProcessPipeline(ShaderProgram program) {
        this.program = program;
    }

    public ShaderProgram getProgram() {
        return program;
    }

    public void setupDefaultUniforms() {
        program.setUniforms(Uniforms.getCollection());
        program.setUniform(new Uniform<>("screenTexture", 0));
        program.setUniform(new Uniform<>("depthTexture", 1));
    }

    /**
     * Override this if the shader needs specific uniforms
     * (like time, blur strength, etc.) before the quad is drawn.
     */
    public void setupUniforms() {
    }
}