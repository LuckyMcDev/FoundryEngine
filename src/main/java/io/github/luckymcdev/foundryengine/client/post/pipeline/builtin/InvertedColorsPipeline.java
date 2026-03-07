package io.github.luckymcdev.foundryengine.client.post.pipeline.builtin;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.common.Common;
import net.minecraft.resources.Identifier;

import java.util.List;

public class InvertedColorsPipeline extends PostProcessPipeline {

    @Override
    public Identifier getName() {
        return Common.id("post_inverted");
    }

    @Override
    public PostProcessStage getInitialStage() {
        return PostProcessStage.FINAL;
    }

    @Override
    public List<PostProcessPipelinePass> getPasses() {
        return List.of(
                new PostProcessPipelinePass(
                        Common.id("post_inverted_pass"),
                        new Shader(ExtendedShaderType.VERTEX,
                                new ShaderSource(
                                        Common.id("post_inverted_vert"),
                                        Common.id("shaders/vert.vsh")
                                )),
                        new Shader(ExtendedShaderType.FRAGMENT,
                                new ShaderSource(
                                        Common.id("post_inverted_frag"),
                                        Common.id("shaders/post/inverted/inverted.fsh")
                                ))
                )
        );
    }
}