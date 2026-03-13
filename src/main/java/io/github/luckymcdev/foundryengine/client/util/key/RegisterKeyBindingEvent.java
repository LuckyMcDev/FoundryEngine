package io.github.luckymcdev.foundryengine.client.util.key;

import net.neoforged.bus.api.Event;

/**
 * Event at which to register {@link KeyBinding} to the {@link KeyBindingManager}
 */
public class RegisterKeyBindingEvent extends Event {
    private final KeyBindingManager manager;

    public RegisterKeyBindingEvent(KeyBindingManager manager) {
        this.manager = manager;
    }

    public KeyBindingManager getKeyBindingManager() {
        return manager;
    }

    public void register(KeyBinding keyBinding) {
        getKeyBindingManager().register(keyBinding);
    }
}
