package io.github.luckymcdev.foundryengine.client.post.pipeline.builtin;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.param.PipelineParam;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;

public class AsciiPostProcessPipeline extends PostProcessPipeline {

    /** Size of each ASCII character cell in pixels. */
    public final PipelineParam<Float> cellSize =
            addParam(PipelineParam.floatParam("cellSize", "Cell Size", 8.0f, 2.0f, 32.0f));

    public AsciiPostProcessPipeline(Identifier name, PostProcessPipelinePass... passes) {
        super(name, passes);
    }

    @Override
    public void setupUniforms(int passIndex, PostProcessPipelinePass pass) {
        // Resolution must be set at runtime since the window can be resized.
        Minecraft mc = Instances.getMinecraft();
        getProgramForPass(passIndex).setUniform(
                new Uniform<>("resolution", new Vector2f(mc.getWindow().getWidth(), mc.getWindow().getHeight()))
        );
    }
}