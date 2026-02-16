package io.github.luckymcdev.foundryengine.client.post.pipeline.builtin;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;

public class AsciiPostProcessPipeline extends PostProcessPipeline {
    public AsciiPostProcessPipeline(Identifier name, PostProcessPipelinePass... shaders) {
        super(name, shaders);
    }

    @Override
    public void setupUniforms(int passIndex) {
        // Get the current window resolution
        Minecraft mc = Instances.getMinecraft();
        float width = mc.getWindow().getWidth();
        float height = mc.getWindow().getHeight();
        // Set resolution uniform for the shader
        getProgram().setUniform(new Uniform<>("resolution", new Vector2f(width, height)));
    }
}