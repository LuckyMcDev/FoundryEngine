package de.luckymcdev.foundryengine.common.game;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Events fired during game session lifecycle transitions.
 */
public abstract class GameSessionEvent extends Event {
    private final Identifier sessionId;

    protected GameSessionEvent(Identifier sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Returns the session ID for this event.
     */
    public Identifier getSessionId() {
        return sessionId;
    }

    public static class Starting extends GameSessionEvent implements ICancellableEvent {
        /**
         * Fired when a session is about to start, cancellable.
         */
        public Starting(Identifier sessionId) {
            super(sessionId);
        }
    }

    public static class Started extends GameSessionEvent {
        /**
         * Fired after a session has started.
         */
        public Started(Identifier sessionId) {
            super(sessionId);
        }
    }

    public static class Stopping extends GameSessionEvent implements ICancellableEvent {
        /**
         * Fired when a session is about to stop, cancellable.
         */
        public Stopping(Identifier sessionId) {
            super(sessionId);
        }
    }

    public static class Stopped extends GameSessionEvent {
        /**
         * Fired after a session has stopped.
         */
        public Stopped(Identifier sessionId) {
            super(sessionId);
        }
    }
}
