package de.luckymcdev.foundryengine.common.game;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionEventTest {

    private static final Identifier SESSION_ID = Identifier.fromNamespaceAndPath("test", "session");

    @Test
    void starting_Event_HasCorrectId() {
        var event = new GameSessionEvent.Starting(SESSION_ID);
        assertEquals(SESSION_ID, event.getSessionId());
    }

    @Test
    void starting_Event_IsCancellable() {
        var event = new GameSessionEvent.Starting(SESSION_ID);
        assertTrue(event instanceof net.neoforged.bus.api.ICancellableEvent);
    }

    @Test
    void starting_Event_NotCancelledByDefault() {
        var event = new GameSessionEvent.Starting(SESSION_ID);
        assertFalse(event.isCanceled());
    }

    @Test
    void starting_Event_CancelWorks() {
        var event = new GameSessionEvent.Starting(SESSION_ID);
        event.setCanceled(true);
        assertTrue(event.isCanceled());
    }

    @Test
    void started_Event_HasCorrectId() {
        var event = new GameSessionEvent.Started(SESSION_ID);
        assertEquals(SESSION_ID, event.getSessionId());
    }

    @Test
    void started_Event_IsNotCancellable() {
        var event = new GameSessionEvent.Started(SESSION_ID);
        assertFalse(event instanceof net.neoforged.bus.api.ICancellableEvent);
    }

    @Test
    void stopping_Event_HasCorrectId() {
        var event = new GameSessionEvent.Stopping(SESSION_ID);
        assertEquals(SESSION_ID, event.getSessionId());
    }

    @Test
    void stopping_Event_IsCancellable() {
        var event = new GameSessionEvent.Stopping(SESSION_ID);
        assertTrue(event instanceof net.neoforged.bus.api.ICancellableEvent);
    }

    @Test
    void stopping_Event_CancelWorks() {
        var event = new GameSessionEvent.Stopping(SESSION_ID);
        event.setCanceled(true);
        assertTrue(event.isCanceled());
    }

    @Test
    void stopped_Event_HasCorrectId() {
        var event = new GameSessionEvent.Stopped(SESSION_ID);
        assertEquals(SESSION_ID, event.getSessionId());
    }

    @Test
    void stopped_Event_IsNotCancellable() {
        var event = new GameSessionEvent.Stopped(SESSION_ID);
        assertFalse(event instanceof net.neoforged.bus.api.ICancellableEvent);
    }

    @Test
    void events_DifferentSubtypes_NotEqual() {
        assertNotEquals(
                new GameSessionEvent.Starting(SESSION_ID).getClass(),
                new GameSessionEvent.Started(SESSION_ID).getClass()
        );
    }
}
