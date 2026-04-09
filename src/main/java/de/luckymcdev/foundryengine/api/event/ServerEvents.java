package de.luckymcdev.foundryengine.api.event;

import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class ServerEvents {
    private static final EventGroup<ServerStartedEvent> STARTUP = new EventGroup<>();

    public static void started(EventCallback<ServerStartedEvent> callback) {
        STARTUP.add(callback);
    }

    public static void _postStarted(ServerStartedEvent event) {
        STARTUP.post(event);
    }
}