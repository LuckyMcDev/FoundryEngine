package io.github.luckymcdev.client.post;

import io.github.luckymcdev.client.TbRenderer;
import io.github.luckymcdev.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.client.opengl.shaders.uniform.Uniforms;

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
    }

    /**
     * Override this if the shader needs specific uniforms
     * (like time, blur strength, etc.) before the quad is drawn.
     */
    public void setupUniforms() {
        program.setUniform(new Uniform<>("screenTexture", 0));
    }
}