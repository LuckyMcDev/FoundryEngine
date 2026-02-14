package io.github.luckymcdev.client.util;

import net.neoforged.bus.api.Event;

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
