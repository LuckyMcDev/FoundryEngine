package de.luckymcdev.foundryengine.common.game.stage;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Events for Game Stages.
 * <br>
 * Look at subclasses. Cannot listen to this as an event.
 */
public abstract class GameStageEvent extends PlayerEvent {
    private final String name;

    protected GameStageEvent(Player player, String name) {
        super(player);
        this.name = name;
    }

    /**
     * Gets the stage name for the event.
     *
     * @return The stage name for the event.
     */
    public String getStageName() {
        return this.name;
    }

    /**
     * Fired before a stage is added.
     * IS cancellable
     */
    public static class Add extends GameStageEvent implements ICancellableEvent {
        public Add(Player player, String name) {
            super(player, name);
        }
    }

    /**
     * Fired before a stage is removed.
     * IS cancellable
     */
    public static class Remove extends GameStageEvent implements ICancellableEvent {
        public Remove(Player player, String name) {
            super(player, name);
        }
    }

    /**
     * Fired after a stage has been added.
     * NOT cancellable
     */
    public static class Added extends GameStageEvent {
        public Added(Player player, String name) {
            super(player, name);
        }
    }

    /**
     * Fired after a stage has been removed.
     * NOT cancellable
     */
    public static class Removed extends GameStageEvent {
        public Removed(Player player, String name) {
            super(player, name);
        }
    }
}
