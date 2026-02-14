package io.github.luckymcdev.client.post;

import io.github.luckymcdev.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.client.opengl.shaders.uniform.Uniform;

public class PostProcessPipeline {
    private final ShaderProgram program;

    public PostProcessPipeline(ShaderProgram program) {
        this.program = program;
    }

    public ShaderProgram getProgram() {
        return program;
    }

    /**
     * Override this if the shader needs specific uniforms
     * (like time, blur strength, etc.) before the quad is drawn.
     */
    public void setupUniforms() {
        program.setUniform(new Uniform<>("screenTexture", 0));
    }
}