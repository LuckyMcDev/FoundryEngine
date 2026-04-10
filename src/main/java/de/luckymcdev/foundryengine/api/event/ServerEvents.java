package de.luckymcdev.foundryengine.api.event;

import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ServerEvents {
    private static final EventGroup<ServerAboutToStartEvent> ABOUT_TO_START = new EventGroup<>();
    private static final EventGroup<ServerStartedEvent> STARTED = new EventGroup<>();
    private static final EventGroup<ServerStartingEvent> STARTING = new EventGroup<>();
    private static final EventGroup<ServerStoppedEvent> STOPPED = new EventGroup<>();
    private static final EventGroup<ServerStoppingEvent> STOPPING = new EventGroup<>();
    private static final EventGroup<ServerTickEvent.Post> TICK = new EventGroup<>();

    public static void aboutToStart(EventCallback<ServerAboutToStartEvent> callback) {
        ABOUT_TO_START.add(callback);
    }

    public static void started(EventCallback<ServerStartedEvent> callback) {
        STARTED.add(callback);
    }

    public static void starting(EventCallback<ServerStartingEvent> callback) {
        STARTING.add(callback);
    }

    public static void stopped(EventCallback<ServerStoppedEvent> callback) {
        STOPPED.add(callback);
    }

    public static void stopping(EventCallback<ServerStoppingEvent> callback) {
        STOPPING.add(callback);
    }

    public static void tick(EventCallback<ServerTickEvent.Post> callback) {
        TICK.add(callback);
    }

    public static void _postAboutToStart(ServerAboutToStartEvent event) {
        ABOUT_TO_START.post(event);
    }

    public static void _postStarted(ServerStartedEvent event) {
        STARTED.post(event);
    }

    public static void _postStarting(ServerStartingEvent event) {
        STARTING.post(event);
    }

    public static void _postStopped(ServerStoppedEvent event) {
        STOPPED.post(event);
    }

    public static void _postStopping(ServerStoppingEvent event) {
        STOPPING.post(event);
    }

    public static void _postTick(ServerTickEvent.Post event) {
        TICK.post(event);
    }
}