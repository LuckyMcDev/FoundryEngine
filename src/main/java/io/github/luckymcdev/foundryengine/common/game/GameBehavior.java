package io.github.luckymcdev.foundryengine.common.game;

import io.github.luckymcdev.foundryengine.common.priority.Priority;

public abstract class GameBehavior {
    public boolean enabled;

    public void onRegister() {
    }

    public void onUnregister() {
    }

    public Priority getPriority() {
        return Priority.NORMAL;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}