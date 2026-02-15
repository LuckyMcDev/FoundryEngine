package io.github.luckymcdev.foundryengine.client.post.staged;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.*;

public enum PostProcessStage {
    // --- Level/World Stages ---
    AFTER_SKY(RenderLevelStageEvent.AfterSky.class),
    AFTER_OPAQUE_BLOCKS(RenderLevelStageEvent.AfterOpaqueBlocks.class),
    AFTER_ENTITIES(RenderLevelStageEvent.AfterEntities.class),
    AFTER_TRANSLUCENT_BLOCKS(RenderLevelStageEvent.AfterTranslucentBlocks.class),
    AFTER_TRIPWIRE_BLOCKS(RenderLevelStageEvent.AfterTripwireBlocks.class),
    AFTER_PARTICLES(RenderLevelStageEvent.AfterParticles.class),
    AFTER_WEATHER(RenderLevelStageEvent.AfterWeather.class),
    AFTER_LEVEL(RenderLevelStageEvent.AfterLevel.class)
    ;

    private final Class<? extends Event> eventClass;

    PostProcessStage(Class<? extends Event> eventClass) {
        this.eventClass = eventClass;
    }
    public Class<? extends Event> getEventClass() { return eventClass; }
}