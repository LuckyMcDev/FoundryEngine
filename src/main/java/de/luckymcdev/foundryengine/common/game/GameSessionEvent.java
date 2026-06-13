package de.luckymcdev.foundryengine.common.game;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class GameSessionEvent extends Event {
    private final Identifier sessionId;

    protected GameSessionEvent(Identifier sessionId) {
        this.sessionId = sessionId;
    }

    public Identifier getSessionId() {
        return sessionId;
    }

    public static class Starting extends GameSessionEvent implements ICancellableEvent {
        public Starting(Identifier sessionId) {
            super(sessionId);
        }
    }

    public static class Started extends GameSessionEvent {
        public Started(Identifier sessionId) {
            super(sessionId);
        }
    }

    public static class Stopping extends GameSessionEvent implements ICancellableEvent {
        public Stopping(Identifier sessionId) {
            super(sessionId);
        }
    }

    public static class Stopped extends GameSessionEvent {
        public Stopped(Identifier sessionId) {
            super(sessionId);
        }
    }
}
