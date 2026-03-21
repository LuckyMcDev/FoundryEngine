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

public class CRTScanlinePipeline extends PostProcessPipeline {

    public final PipelineParam<Float> framesPerHz =
            addParam(PipelineParam.floatParam("framesPerHz", "Frames Per Hz", 2.0f, 1.0f, 100.0f));

    public final PipelineParam<Float> gain =
            addParam(PipelineParam.floatParam("gain", "Brightness Gain", 0.5f, 0.1f, 1.0f));

    @Override
    public Identifier getName() {
        return Common.id("post_crt_bfi");
    }

    @Override
    public PostProcessStage getInitialStage() {
        return PostProcessStage.FINAL;
    }

    @Override
    public List<PostProcessPipelinePass> getPasses() {
        return List.of(
                new PostProcessPipelinePass(
                        Common.id("post_crt_bfi_pass"),
                        new Shader(ExtendedShaderType.VERTEX,
                                new ShaderSource(Common.id("post_crt_vert"), Common.id("shaders/vert.vsh"))),
                        new Shader(ExtendedShaderType.FRAGMENT,
                                new ShaderSource(Common.id("post_crt_frag"), Common.id("shaders/post/crt_bfi/crt_bfi.fsh")))
                )
        );
    }

    @Override
    public void setupUniforms(PostProcessPipelinePass pass, ShaderProgram program) {
        Minecraft mc = Client.getMinecraft();

        program.setUniform(new Uniform<>("resolution", () ->
                new Vector2f(mc.getWindow().getWidth(), mc.getWindow().getHeight())));

        // frameCount is used to move the "rolling scan"
        program.setUniform(new Uniform<>("frameCount", mc.levelRenderer::getTicks));
    }
}