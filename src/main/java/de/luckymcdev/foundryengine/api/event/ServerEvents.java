package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;

public class ServerEvents {
    public static final EventGroupHolder<ServerAboutToStartEvent> ABOUT_TO_START =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_ABOUT_TO_START);
    public static final EventGroupHolder<ServerStartedEvent> STARTED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STARTED);
    public static final EventGroupHolder<ServerStartingEvent> STARTING =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STARTING);
    public static final EventGroupHolder<ServerStoppedEvent> STOPPED =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STOPPED);
    public static final EventGroupHolder<ServerStoppingEvent> STOPPING =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_STOPPING);
    public static final EventGroupHolder<ServerTickEvent.Post> TICK =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_TICK, BlueprintContexts::serverTick);
    public static final EventGroupHolder<TagsUpdatedEvent> TAGS =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_SERVER_TAGS);

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
        public static void postAboutToStart(ServerAboutToStartEvent e) {
            ABOUT_TO_START.post(e);
        }

        public static void postStarted(ServerStartedEvent e) {
            STARTED.post(e);
        }

        public static void postStarting(ServerStartingEvent e) {
            STARTING.post(e);
        }

        public static void postStopped(ServerStoppedEvent e) {
            STOPPED.post(e);
        }

        public static void postStopping(ServerStoppingEvent e) {
            STOPPING.post(e);
        }

        public static void postTick(ServerTickEvent.Post e) {
            TICK.post(e);
        }

        public static void postTags(TagsUpdatedEvent e) {
            TAGS.post(e);
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