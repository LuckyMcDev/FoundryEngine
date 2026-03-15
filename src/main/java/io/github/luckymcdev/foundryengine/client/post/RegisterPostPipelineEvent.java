package io.github.luckymcdev.foundryengine.client.post;

import io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

/**
 * Simple Event to register {@link PostProcessPipeline} to the {@link PostProcessManager}
 */
@ApiStatus.Experimental
public class RegisterPostPipelineEvent extends Event {
    private final PostProcessManager manager;

    public RegisterPostPipelineEvent(PostProcessManager manager) {
        this.manager = manager;
    }

    public void register(PostProcessPipeline pipeline) {
        manager.addPipeline(pipeline);
    }
}
