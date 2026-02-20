package io.github.luckymcdev.foundryengine.client.post.pipeline.builtin;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.TargetRef;
import io.github.luckymcdev.foundryengine.common.Commons;

/**
 * An Example PostProcessPipeline which visualizes the Depth Texture linearly.
 */
public class DepthVisualizePipeline extends PostProcessPipeline {

    /**
     * No Args Constructor.
     */
    public DepthVisualizePipeline() {
        super(
                Commons.id("post_depth_visualize"),
                new PostProcessPipelinePass(
                        Commons.id("post_depth_visualize_pass"),
                        TargetRef.MAIN, TargetRef.MAIN,
                        new Shader(ExtendedShaderType.VERTEX,
                                new ShaderSource(
                                        Commons.id("post_depth_visualize_vert"),
                                        Commons.id("shaders/post/depth/depth_visualize.vsh")
                                )),
                        new Shader(ExtendedShaderType.FRAGMENT,
                                new ShaderSource(
                                        Commons.id("post_depth_visualize_frag"),
                                        Commons.id("shaders/post/depth/depth_visualize.fsh")
                                ))
                )
        );
    }
}