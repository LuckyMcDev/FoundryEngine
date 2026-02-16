package io.github.luckymcdev.foundryengine.client.render;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.AsciiPostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PostProcessPipelinePass;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.StagedPostProcessPipeline;
import io.github.luckymcdev.foundryengine.common.Commons;
import org.slf4j.Logger;

public class TestRender {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void registerPipelines(RegisterPostPipelineEvent event) {
        try {
            LOGGER.info("Registering post-processing pipelines...");

            registerGrayscale(event);
            registerDepthVisualize(event);
            registerAscii(event);

        } catch (ShaderException e) {
            LOGGER.error("Failed to register post-processing pipelines", e);
            if (e.getGlError() != null) {
                LOGGER.error("OpenGL error: {}", e.getGlError());
            }
            throw new RuntimeException("Failed to initialize post-processing", e);
        }
    }

    private static void registerGrayscale(RegisterPostPipelineEvent event) throws ShaderException {
        // Create grayscale shaders
        Shader grayScaleVertex = new Shader(
                ExtendedShaderType.VERTEX,
                new ShaderSource(
                        Commons.id("post_grayscale_vert"),
                        Commons.id("shaders/post/grayscale/grayscale.vsh")
                )
        );
        Shader grayScaleFragment = new Shader(
                ExtendedShaderType.FRAGMENT,
                new ShaderSource(
                        Commons.id("post_grayscale_frag"),
                        Commons.id("shaders/post/grayscale/grayscale.fsh")
                )
        );

        // Create pipeline pass
        PostProcessPipelinePass grayscalePass = new PostProcessPipelinePass(
                Commons.id("post_grayscale_pass"),
                grayScaleVertex,
                grayScaleFragment
        );

        // Create and register staged pipeline
        StagedPostProcessPipeline grayScalePipeline = new StagedPostProcessPipeline(
                Commons.id("post_grayscale"),
                PostProcessStage.AFTER_ENTITIES,
                grayscalePass
        );
        event.register(grayScalePipeline);
    }


    private static void registerDepthVisualize(RegisterPostPipelineEvent event) throws ShaderException {
        // Create depth visualize shaders
        Shader depthVisualizeVert = new Shader(
                ExtendedShaderType.VERTEX,
                new ShaderSource(
                        Commons.id("post_depth_visualize_vert"),
                        Commons.id("shaders/post/depth/depth_visualize.vsh")
                )
        );
        Shader depthVisualizeFrag = new Shader(
                ExtendedShaderType.FRAGMENT,
                new ShaderSource(
                        Commons.id("post_depth_visualize_frag"),
                        Commons.id("shaders/post/depth/depth_visualize.fsh")
                )
        );

        // Create pipeline pass
        PostProcessPipelinePass depthVisualizePass = new PostProcessPipelinePass(
                Commons.id("post_depth_visualize_pass"),
                depthVisualizeVert,
                depthVisualizeFrag
        );

        // Create and register pipeline
        PostProcessPipeline depthVisualize = new PostProcessPipeline(
                Commons.id("post_depth_visualize"),
                depthVisualizePass
        );
        event.register(depthVisualize);
    }

    private static void registerAscii(RegisterPostPipelineEvent event) throws ShaderException {
        // Create ascii shaders
        Shader asciiVert = new Shader(
                ExtendedShaderType.VERTEX,
                new ShaderSource(
                        Commons.id("post_ascii_vert"),
                        Commons.id("shaders/post/ascii/ascii.vsh")
                )
        );
        Shader asciiFrag = new Shader(
                ExtendedShaderType.FRAGMENT,
                new ShaderSource(
                        Commons.id("post_ascii_frag"),
                        Commons.id("shaders/post/ascii/ascii.fsh")
                )
        );

        // Create pipeline pass
        PostProcessPipelinePass asciiPass = new PostProcessPipelinePass(
                Commons.id("post_ascii_pass"),
                asciiVert,
                asciiFrag
        );

        // Create and register pipeline
        AsciiPostProcessPipeline asciiPipeline = new AsciiPostProcessPipeline(
                Commons.id("post_ascii"),
                asciiPass
        );
        event.register(asciiPipeline);
    }
}