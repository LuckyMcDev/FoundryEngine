package io.github.luckymcdev.foundryengine.client.render;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.AsciiPostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.pass.PassTarget;
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
        Shader vert = new Shader(
                ExtendedShaderType.VERTEX,
                new ShaderSource(
                        Commons.id("post_grayscale_vert"),
                        Commons.id("shaders/post/grayscale/grayscale.vsh")
                )
        );
        Shader frag = new Shader(
                ExtendedShaderType.FRAGMENT,
                new ShaderSource(
                        Commons.id("post_grayscale_frag"),
                        Commons.id("shaders/post/grayscale/grayscale.fsh")
                )
        );

        PostProcessPipelinePass pass = new PostProcessPipelinePass(
                Commons.id("post_grayscale_pass"),
                PassTarget.MAIN, PassTarget.MAIN,
                vert, frag
        );

        StagedPostProcessPipeline pipeline = new StagedPostProcessPipeline(
                Commons.id("post_grayscale"),
                PostProcessStage.AFTER_ENTITIES,
                pass
        );
        event.register(pipeline);
    }

    private static void registerDepthVisualize(RegisterPostPipelineEvent event) throws ShaderException {
        Shader vert = new Shader(
                ExtendedShaderType.VERTEX,
                new ShaderSource(
                        Commons.id("post_depth_visualize_vert"),
                        Commons.id("shaders/post/depth/depth_visualize.vsh")
                )
        );
        Shader frag = new Shader(
                ExtendedShaderType.FRAGMENT,
                new ShaderSource(
                        Commons.id("post_depth_visualize_frag"),
                        Commons.id("shaders/post/depth/depth_visualize.fsh")
                )
        );

        PostProcessPipelinePass pass = new PostProcessPipelinePass(
                Commons.id("post_depth_visualize_pass"),
                PassTarget.MAIN, PassTarget.MAIN,
                vert, frag
        );

        PostProcessPipeline pipeline = new PostProcessPipeline(
                Commons.id("post_depth_visualize"),
                pass
        );
        event.register(pipeline);
    }

    private static void registerAscii(RegisterPostPipelineEvent event) throws ShaderException {
        Shader vert = new Shader(
                ExtendedShaderType.VERTEX,
                new ShaderSource(
                        Commons.id("post_ascii_vert"),
                        Commons.id("shaders/post/ascii/ascii.vsh")
                )
        );
        Shader frag = new Shader(
                ExtendedShaderType.FRAGMENT,
                new ShaderSource(
                        Commons.id("post_ascii_frag"),
                        Commons.id("shaders/post/ascii/ascii.fsh")
                )
        );

        PostProcessPipelinePass pass = new PostProcessPipelinePass(
                Commons.id("post_ascii_pass"),
                PassTarget.MAIN, PassTarget.MAIN,
                vert, frag
        );

        AsciiPostProcessPipeline pipeline = new AsciiPostProcessPipeline(
                Commons.id("post_ascii"),
                pass
        );
        event.register(pipeline);
    }
}