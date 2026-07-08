package de.luckymcdev.foundryengine.common.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

	@Test
	void simpleState_Lobby_IsActive() {
		assertTrue(SimpleState.LOBBY.isActive());
	}

	@Test
	void simpleState_Playing_IsActive() {
		assertTrue(SimpleState.PLAYING.isActive());
	}

	@Test
	void simpleState_Finished_NotActive() {
		assertFalse(SimpleState.FINISHED.isActive());
	}

	@Test
	void simpleState_Stopped_NotActive() {
		assertFalse(SimpleState.STOPPED.isActive());
	}

	@Test
	void customState_ImplementsInterface() {
		GameState custom = () -> true;
		assertTrue(custom.isActive());
	}
}
