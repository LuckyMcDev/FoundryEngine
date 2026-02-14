package io.github.luckymcdev.client.render;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.client.opengl.shaders.Shader;
import io.github.luckymcdev.client.opengl.shaders.ShaderSource;
import io.github.luckymcdev.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.client.post.PostProcessManager;
import io.github.luckymcdev.client.post.PostProcessPipeline;
import io.github.luckymcdev.common.Commons;
import org.slf4j.Logger;

public class TestRender {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized = false;

    public static void registerPipelines() {
        if (initialized) {
            LOGGER.warn("TestRender.registerPipelines() called multiple times!");
            return;
        }
        initialized = true;

        try {
            LOGGER.info("Registering post-processing pipelines...");

            // Create grayscale shader
            Shader grayScaleFragment = new Shader(
                    ExtendedShaderType.FRAGMENT,
                    new ShaderSource(
                            Commons.id("post_grayscale_frag"),
                            Commons.id("shaders/post_grayscale.fsh")
                    )
            );

            Shader grayScaleVertex = new Shader(
                    ExtendedShaderType.VERTEX,
                    new ShaderSource(
                            Commons.id("post_grayscale_vert"),
                            Commons.id("shaders/post_grayscale.vsh")
                    )
            );

            ShaderProgram grayScaleProgram = new ShaderProgram(
                    Commons.id("post_grayscale"),
                    grayScaleFragment,
                    grayScaleVertex
            );
            grayScaleProgram.link();

            // Create and register pipeline
            PostProcessPipeline grayScalePipeline = new PostProcessPipeline(grayScaleProgram);
            PostProcessManager.addPipeline(grayScalePipeline);

            LOGGER.info("Successfully registered grayscale post-processing pipeline");

        } catch (ShaderException e) {
            LOGGER.error("Failed to register post-processing pipelines", e);
            if (e.getGlError() != null) {
                LOGGER.error("OpenGL error: {}", e.getGlError());
            }
            throw new RuntimeException("Failed to initialize post-processing", e);
        }
    }
}