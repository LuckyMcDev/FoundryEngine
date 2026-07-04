package de.luckymcdev.foundryengine.common.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void isStarting_Starting_True() {
        assertTrue(GameState.STARTING.isStarting());
    }

    @Test
    void isStarting_Other_False() {
        assertFalse(GameState.RUNNING.isStarting());
        assertFalse(GameState.STOPPING.isStarting());
        assertFalse(GameState.STOPPED.isStarting());
    }

    @Test
    void isRunning_Running_True() {
        assertTrue(GameState.RUNNING.isRunning());
    }

    @Test
    void isRunning_Other_False() {
        assertFalse(GameState.STARTING.isRunning());
        assertFalse(GameState.STOPPING.isRunning());
        assertFalse(GameState.STOPPED.isRunning());
    }

    @Test
    void isStopping_Stopping_True() {
        assertTrue(GameState.STOPPING.isStopping());
    }

    @Test
    void isStopping_Other_False() {
        assertFalse(GameState.STARTING.isStopping());
        assertFalse(GameState.RUNNING.isStopping());
        assertFalse(GameState.STOPPED.isStopping());
    }

    @Test
    void isStopped_Stopped_True() {
        assertTrue(GameState.STOPPED.isStopped());
    }

    @Test
    void isStopped_Other_False() {
        assertFalse(GameState.STARTING.isStopped());
        assertFalse(GameState.RUNNING.isStopped());
        assertFalse(GameState.STOPPING.isStopped());
    }

    @Test
    void stateOrder_MatchesLifecycle() {
        assertEquals(0, GameState.STARTING.ordinal());
        assertEquals(1, GameState.RUNNING.ordinal());
        assertEquals(2, GameState.STOPPING.ordinal());
        assertEquals(3, GameState.STOPPED.ordinal());
    }
}
