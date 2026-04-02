package de.luckymcdev.foundryengine.client.post.pipeline.staged;

import de.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * An Enum which contains all the Possible Stages for a {@link PostProcessPipeline} to render at.
 * Will be expanded with more Possible Stages soon.
 */
public enum PostProcessStage {
    AFTER_SKY(RenderLevelStageEvent.AfterSky.class),
    AFTER_OPAQUE_BLOCKS(RenderLevelStageEvent.AfterOpaqueBlocks.class),
    AFTER_OPAQUE_FEATURES(RenderLevelStageEvent.AfterOpaqueFeatures.class),
    AFTER_TRANSLUCENT_FEATURES(RenderLevelStageEvent.AfterTranslucentFeatures.class),
    AFTER_TRANSLUCENT_BLOCKS(RenderLevelStageEvent.AfterTranslucentBlocks.class),
    AFTER_TRANSLUCENT_PARTICLES(RenderLevelStageEvent.AfterTranslucentParticles.class),
    AFTER_WEATHER(RenderLevelStageEvent.AfterWeather.class),
    AFTER_LEVEL(RenderLevelStageEvent.AfterLevel.class),
    FINAL(RenderLevelStageEvent.AfterLevel.class),
    AFTER_GUI(RenderGuiEvent.Post.class);

    private final Class<? extends Event> eventClass;

    PostProcessStage(Class<? extends Event> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends Event> getEventClass() {
        return eventClass;
    }
}