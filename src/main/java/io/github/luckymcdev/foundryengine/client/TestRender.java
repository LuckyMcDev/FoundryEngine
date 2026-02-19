package io.github.luckymcdev.foundryengine.client;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.post.RegisterPostPipelineEvent;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.AsciiPostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.DepthVisualizePipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.builtin.GrayscalePipeline;
import org.slf4j.Logger;

public class TestRender {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void registerPipelines(RegisterPostPipelineEvent event) {
        LOGGER.info("Registering post-processing pipelines...");

        event.register(new GrayscalePipeline());
        event.register(new DepthVisualizePipeline());
        event.register(new AsciiPostProcessPipeline());
    }
}