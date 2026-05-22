package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;

public class ServerEvents {
    public static final EventGroupHolder<ServerAboutToStartEvent> ABOUT_TO_START =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_ABOUT_TO_START, BlueprintContexts::serverAboutToStart);
    public static final EventGroupHolder<ServerStartedEvent> STARTED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STARTED, BlueprintContexts::serverStarted);
    public static final EventGroupHolder<ServerStartingEvent> STARTING =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STARTING, BlueprintContexts::serverStarting);
    public static final EventGroupHolder<ServerStoppedEvent> STOPPED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STOPPED, BlueprintContexts::serverStopped);
    public static final EventGroupHolder<ServerStoppingEvent> STOPPING =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STOPPING, BlueprintContexts::serverStopping);
    public static final EventGroupHolder<ServerTickEvent.Post> TICK =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_TICK, BlueprintContexts::serverTick);
    public static final EventGroupHolder<TagsUpdatedEvent> TAGS =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_TAGS, BlueprintContexts::serverTags);

    public static void aboutToStart(EventCallback<ServerAboutToStartEvent> cb) {
        ABOUT_TO_START.register(cb);
    }

    public static void started(EventCallback<ServerStartedEvent> cb) {
        STARTED.register(cb);
    }

    public static void starting(EventCallback<ServerStartingEvent> cb) {
        STARTING.register(cb);
    }

    public static void stopped(EventCallback<ServerStoppedEvent> cb) {
        STOPPED.register(cb);
    }

    public static void stopping(EventCallback<ServerStoppingEvent> cb) {
        STOPPING.register(cb);
    }

    public static void tick(EventCallback<ServerTickEvent.Post> cb) {
        TICK.register(cb);
    }

    public static void tags(EventCallback<TagsUpdatedEvent> cb) {
        TAGS.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {

        public static void register(IEventBus bus) {
            bus.addListener(ABOUT_TO_START::post);
            bus.addListener(STARTED::post);
            bus.addListener(STARTING::post);
            bus.addListener(STOPPED::post);
            bus.addListener(STOPPING::post);
            bus.addListener(TICK::post);
            bus.addListener(TAGS::post);
        }

        public static void clear() {
            ABOUT_TO_START.clear();
            STARTED.clear();
            STARTING.clear();
            STOPPED.clear();
            STOPPING.clear();
            TICK.clear();
            TAGS.clear();
        }
    }
}