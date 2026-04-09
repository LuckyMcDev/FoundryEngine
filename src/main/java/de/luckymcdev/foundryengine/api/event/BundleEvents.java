package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;

public class BundleEvents {
    private static final EventGroup<RegistryEvent> LOAD = new EventGroup<>();

    public static void registry(EventCallback<RegistryEvent> callback) {
        LOAD.add(callback);
    }

    public static void _postRegistry(RegistryEvent event) {
        LOAD.post(event);
    }
}