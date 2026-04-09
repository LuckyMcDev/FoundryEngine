package de.luckymcdev.foundryengine.api.event;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;

public class ClientEvents {
    private static final EventGroup<ClientTickEvent.Post> TICK = new EventGroup<>();
    private static final EventGroup<ClientStoppedEvent> STOPPED = new EventGroup<>();
    private static final EventGroup<ClientStoppingEvent> STOPPING = new EventGroup<>();

    public static void tick(EventCallback<ClientTickEvent.Post> event) {
        TICK.add(event);
    }

    public static void stopped(EventCallback<ClientStoppedEvent> event) {
        STOPPED.add(event);
    }

    public static void stopping(EventCallback<ClientStoppingEvent> event) {
        STOPPING.add(event);
    }

    public static void _postTick(ClientTickEvent.Post event) {
        TICK.post(event);
    }

    public static void _postStopped(ClientStoppedEvent event) {
        STOPPED.post(event);
    }

    public static void _postStopping(ClientStoppingEvent event) {
        STOPPING.post(event);
    }
}