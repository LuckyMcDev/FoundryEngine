package io.github.luckymcdev.foundryengine.client.post.pipeline.builtin;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.common.Common;

/**
 * An example StagedPostProcessPipeline which showcases a grayscale effect at different stages.
 */
public class GrayscalePipeline extends PostProcessPipeline {

    /**
     * No Args Constructor.
     */
    public GrayscalePipeline() {
        super(
                Common.id("post_grayscale"),
                PostProcessStage.AFTER_ENTITIES,
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