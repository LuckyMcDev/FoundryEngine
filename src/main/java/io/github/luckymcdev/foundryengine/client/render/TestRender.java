package io.github.luckymcdev.foundryengine.client.render;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.post.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.staged.PostProcessStage;
import io.github.luckymcdev.foundryengine.client.post.staged.StagedPostProcessPipeline;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import org.slf4j.Logger;

public class TestRender {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void registerPipelines() {
        try {
            LOGGER.info("Registering post-processing pipelines...");

            registerGrayscale();
            registerDepthVisualize();

        } catch (ShaderException e) {
            LOGGER.error("Failed to register post-processing pipelines", e);
            if (e.getGlError() != null) {
                LOGGER.error("OpenGL error: {}", e.getGlError());
            }
            throw new RuntimeException("Failed to initialize post-processing", e);
        }
    }

    private static void registerGrayscale() throws ShaderException {
        // Create grayscale shader
        Shader grayScaleFragment = new Shader(
                ExtendedShaderType.FRAGMENT,
                new ShaderSource(
                        Commons.id("post_grayscale_frag"),
                        Commons.id("shaders/post/grayscale.fsh")
                )
        );

        Instances.getShaderManager().register(grayScaleFragment);

        Shader grayScaleVertex = new Shader(
                ExtendedShaderType.VERTEX,
                new ShaderSource(
                        Commons.id("post_grayscale_vert"),
                        Commons.id("shaders/post/grayscale.vsh")
                )
        );

        Instances.getShaderManager().register(grayScaleVertex);

        ShaderProgram grayScaleProgram = new ShaderProgram(
                Commons.id("post_grayscale"),
                grayScaleFragment,
                grayScaleVertex
        );
        grayScaleProgram.link();

        Instances.getShaderManager().register(grayScaleProgram);

        // Create and register pipeline
        StagedPostProcessPipeline grayScalePipeline = new StagedPostProcessPipeline(PostProcessStage.AFTER_SKY, grayScaleProgram);
        Instances.getPostProcessManager().addPipeline(grayScalePipeline);
    }


    private static void registerDepthVisualize() throws ShaderException {
        // Create grayscale shader
        Shader depthVisualizeFrag = new Shader(
                ExtendedShaderType.FRAGMENT,
                new ShaderSource(
                        Commons.id("post_depth_visualize_frag"),
                        Commons.id("shaders/post/depth_visualize.fsh")
                )
        );

        Instances.getShaderManager().register(depthVisualizeFrag);

        Shader depthVisualizeVert = new Shader(
                ExtendedShaderType.VERTEX,
                new ShaderSource(
                        Commons.id("post_depth_visualize_vert"),
                        Commons.id("shaders/post/depth_visualize.vsh")
                )
        );

        Instances.getShaderManager().register(depthVisualizeVert);

        ShaderProgram depthVisualizeProgram = new ShaderProgram(
                Commons.id("post_depth_visualize"),
                depthVisualizeFrag,
                depthVisualizeVert
        );
        depthVisualizeProgram.link();

        Instances.getShaderManager().register(depthVisualizeProgram);

        // Create and register pipeline
        PostProcessPipeline grayScalePipeline = new PostProcessPipeline(depthVisualizeProgram);
        Instances.getPostProcessManager().addPipeline(grayScalePipeline);
    }
}