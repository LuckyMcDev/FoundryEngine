package io.github.luckymcdev.foundryengine.client.post.pipeline.staged;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * An Enum which contains all the Possible Stages for a {@link io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline} to render at.
 * Will be expanded with more Possible Stages soon.
 */
public enum PostProcessStage {
    AFTER_SKY(RenderLevelStageEvent.AfterSky.class),
    AFTER_OPAQUE_BLOCKS(RenderLevelStageEvent.AfterOpaqueBlocks.class),
    AFTER_TRANSLUCENT_BLOCKS(RenderLevelStageEvent.AfterTranslucentBlocks.class),
    AFTER_WEATHER(RenderLevelStageEvent.AfterWeather.class),
    AFTER_LEVEL(RenderLevelStageEvent.AfterLevel.class),
    FINAL(RenderLevelStageEvent.AfterLevel.class);

    private final Class<? extends Event> eventClass;

    PostProcessStage(Class<? extends Event> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends Event> getEventClass() {
        return eventClass;
    }
}