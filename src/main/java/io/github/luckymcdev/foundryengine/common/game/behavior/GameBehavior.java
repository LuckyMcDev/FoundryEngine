package io.github.luckymcdev.foundryengine.common.game.behavior;

import io.github.luckymcdev.foundryengine.common.priority.Priority;

/**
 * Represents a Game Modification type deal.
 * <br>
 * No good description yet.
 */
public abstract class GameBehavior {
    protected boolean enabled;

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