package io.github.luckymcdev.foundryengine.client.post.pipeline.builtin;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.param.PipelineParam;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.TargetRef;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

public class AsciiPostProcessPipeline extends PostProcessPipeline {

    public AsciiPostProcessPipeline() {
        super(
                Commons.id("post_ascii"),
                new PostProcessPipelinePass(
                        Commons.id("post_ascii_pass"),
                        TargetRef.MAIN, TargetRef.MAIN,
                        new Shader(ExtendedShaderType.VERTEX,
                                new ShaderSource(
                                        Commons.id("post_ascii_vert"),
                                        Commons.id("shaders/post/ascii/ascii.vsh")
                                )),
                        new Shader(ExtendedShaderType.FRAGMENT,
                                new ShaderSource(
                                        Commons.id("post_ascii_frag"),
                                        Commons.id("shaders/post/ascii/ascii.fsh")
                                ))
                )
        );
    }

    /** Size of each ASCII character cell in pixels. */
    public final PipelineParam<Integer> cellSize =
            addParam(PipelineParam.intParam("cellSize", "Cell Size", 8, 2, 32));

    @Override
    public void setupUniforms(int passIndex, PostProcessPipelinePass pass) {
        Minecraft mc = Instances.getMinecraft();
        getProgramForPass(passIndex).setUniform(
                new Uniform<>("resolution", new Vector2f(mc.getWindow().getWidth(), mc.getWindow().getHeight()))
        );
    }
}