package io.github.luckymcdev.foundryengine.client.post.pipeline.builtin;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.TargetRef;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.StagedPostProcessPipeline;
import io.github.luckymcdev.foundryengine.common.Commons;

/**
 * An example StagedPostProcessPipeline which showcases a grayscale effect at different stages.
 */
public class GrayscalePipeline extends StagedPostProcessPipeline {

    /**
     * No Args Constructor.
     */
    public GrayscalePipeline() {
        super(
                Commons.id("post_grayscale"),
                PostProcessStage.AFTER_ENTITIES,
                new PostProcessPipelinePass(
                        Commons.id("post_grayscale_pass"),
                        TargetRef.MAIN, TargetRef.MAIN,
                        new Shader(ExtendedShaderType.VERTEX,
                                new ShaderSource(
                                        Commons.id("post_grayscale_vert"),
                                        Commons.id("shaders/vert.vsh")
                                )),
                        new Shader(ExtendedShaderType.FRAGMENT,
                                new ShaderSource(
                                        Commons.id("post_grayscale_frag"),
                                        Commons.id("shaders/post/grayscale/grayscale.fsh")
                                ))
                )
        );
    }
}