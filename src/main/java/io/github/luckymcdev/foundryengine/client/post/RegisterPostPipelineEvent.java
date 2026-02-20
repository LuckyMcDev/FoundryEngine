package io.github.luckymcdev.foundryengine.client.post;

import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import io.github.luckymcdev.foundryengine.client.post.pipeline.staged.StagedPostProcessPipeline;
import net.neoforged.bus.api.Event;

/**
 * Simple Event to register {@link PostProcessPipeline} to the {@link PostProcessManager}
 */
public class RegisterPostPipelineEvent extends Event {
    private final PostProcessManager MANAGER;

    public RegisterPostPipelineEvent(PostProcessManager manager) {
        MANAGER = manager;
    }

    public void register(PostProcessPipeline pipeline) {
        MANAGER.addPipeline(pipeline);
    }

    public void register(StagedPostProcessPipeline pipeline) {
        MANAGER.addPipeline(pipeline);
    }
}
