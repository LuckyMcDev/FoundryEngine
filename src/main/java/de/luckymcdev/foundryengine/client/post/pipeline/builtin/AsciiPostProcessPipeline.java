package de.luckymcdev.foundryengine.client.post.pipeline.builtin;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.opengl.program.ShaderProgram;
import de.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import de.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import de.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import de.luckymcdev.foundryengine.client.opengl.uniform.Uniform;
import de.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import de.luckymcdev.foundryengine.client.post.pipeline.param.PipelineParam;
import de.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import de.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;

import java.util.List;

/**
 * An example PostProcessPipeline which renders Minecraft as ASCII.
 */
public class AsciiPostProcessPipeline extends PostProcessPipeline {

    /**
     * Size of each ASCII character cell in pixels.
     */
    public final PipelineParam<Integer> cellSize =
            addParam(PipelineParam.intParam("cellSize", "Cell Size", 8, 2, 32));

    @Override
    public Identifier getName() {
        return Common.id("post_ascii");
    }

    @Override
    public PostProcessStage getInitialStage() {
        return PostProcessStage.FINAL;
    }

    @Override
    public List<PostProcessPipelinePass> getPasses() {
        return List.of(
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