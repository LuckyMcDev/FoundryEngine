package io.github.luckymcdev.foundryengine.client.post.pipeline.builtin;

import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.param.PipelineParam;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

/**
 * An example PostProcessPipeline which renders Minecraft as ASCII.
 */
public class AsciiPostProcessPipeline extends PostProcessPipeline {

    /**
     * Size of each ASCII character cell in pixels.
     */
    public final PipelineParam<Integer> cellSize =
            addParam(PipelineParam.intParam("cellSize", "Cell Size", 8, 2, 32));

    /**
     * No Args Constructor.
     */
    public AsciiPostProcessPipeline() {
        super(
                Common.id("post_ascii"),
                PostProcessStage.FINAL,
                new PostProcessPipelinePass(
                        Common.id("post_ascii_pass"),
                        new Shader(ExtendedShaderType.VERTEX,
                                new ShaderSource(
                                        Common.id("post_ascii_vert"),
                                        Common.id("shaders/vert.vsh")
                                )),
                        new Shader(ExtendedShaderType.FRAGMENT,
                                new ShaderSource(
                                        Common.id("post_ascii_frag"),
                                        Common.id("shaders/post/ascii/ascii.fsh")
                                ))
                )
        );
    }

    @Override
    public void setupUniforms(PostProcessPipelinePass pass, ShaderProgram program) {
        Minecraft mc = Client.getMinecraft();
        program.setUniform(new Uniform<>("resolution", () -> new Vector2f(mc.getWindow().getWidth(), mc.getWindow().getHeight())));
    }
}