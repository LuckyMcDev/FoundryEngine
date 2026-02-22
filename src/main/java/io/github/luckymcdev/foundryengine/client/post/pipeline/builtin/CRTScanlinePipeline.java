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
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.TargetRef;
import io.github.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;

public class CRTScanlinePipeline extends PostProcessPipeline {

    public final PipelineParam<Float> framesPerHz =
            addParam(PipelineParam.floatParam("framesPerHz", "Frames Per Hz", 2.0f, 1.0f, 100.0f));

    public final PipelineParam<Float> gain =
            addParam(PipelineParam.floatParam("gain", "Brightness Gain", 0.5f, 0.1f, 1.0f));

    public CRTScanlinePipeline() {
        super(
                Common.id("post_crt_bfi"),
                new PostProcessPipelinePass(
                        Common.id("post_crt_bfi_pass"),
                        TargetRef.MAIN, TargetRef.MAIN,
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