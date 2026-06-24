package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;

public class ServerEvents {
    public static final EventGroupHolder<ServerAboutToStartEvent> ABOUT_TO_START = new EventGroupHolder<>();
    public static final EventGroupHolder<ServerStartedEvent> STARTED = new EventGroupHolder<>();
    public static final EventGroupHolder<ServerStartingEvent> STARTING = new EventGroupHolder<>();
    public static final EventGroupHolder<ServerStoppedEvent> STOPPED = new EventGroupHolder<>();
    public static final EventGroupHolder<ServerStoppingEvent> STOPPING = new EventGroupHolder<>();
    public static final EventGroupHolder<ServerTickEvent.Post> TICK = new EventGroupHolder<>();
    public static final EventGroupHolder<TagsUpdatedEvent> TAGS = new EventGroupHolder<>();

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
        static {
            Common.registerEventClear(Internal::clear);
        }

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

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postAboutToStart);
            bus.addListener(Internal::postStarted);
            bus.addListener(Internal::postStarting);
            bus.addListener(Internal::postStopped);
            bus.addListener(Internal::postStopping);
            bus.addListener(Internal::postTick);
            bus.addListener(Internal::postTags);
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
