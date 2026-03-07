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

/**
 * An example StagedPostProcessPipeline which showcases a grayscale effect at different stages.
 */
public class GrayscalePipeline extends PostProcessPipeline {
    @Override
    public Identifier getName() {
        return Common.id("post_grayscale");
    }

    @Override
    public PostProcessStage getInitialStage() {
        return PostProcessStage.AFTER_ENTITIES;
    }

    @Override
    public List<PostProcessPipelinePass> getPasses() {
        return List.of(
                new PostProcessPipelinePass(
                        Common.id("post_grayscale_pass"),
                        new Shader(ExtendedShaderType.VERTEX,
                                new ShaderSource(
                                        Common.id("post_grayscale_vert"),
                                        Common.id("shaders/vert.vsh")
                                )),
                        new Shader(ExtendedShaderType.FRAGMENT,
                                new ShaderSource(
                                        Common.id("post_grayscale_frag"),
                                        Common.id("shaders/post/grayscale/grayscale.fsh")
                                ))
                )
        );
    }
}