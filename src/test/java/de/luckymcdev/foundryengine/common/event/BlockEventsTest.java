package de.luckymcdev.foundryengine.common.event;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class BlockEventsTest {

	@Test
	void broken_RegisterAndClear() {
		AtomicBoolean called = new AtomicBoolean(false);
		BlockEvents.broken(event -> called.set(true));
		assertDoesNotThrow(() -> BlockEvents.Internal.postBroken(null));
		BlockEvents.Internal.clear();
		BlockEvents.Internal.postBroken(null);
	}

	@Test
	void placed_RegisterAndClear() {
		AtomicBoolean called = new AtomicBoolean(false);
		BlockEvents.placed(event -> called.set(true));
		BlockEvents.Internal.clear();
	}

	@Test
	void leftClicked_RegisterAndClear() {
		BlockEvents.leftClicked(event -> {
		});
		BlockEvents.Internal.clear();
	}

	@Test
	void rightClicked_RegisterAndClear() {
		BlockEvents.rightClicked(event -> {
		});
		BlockEvents.Internal.clear();
	}

	@Test
	void farmlandTrampled_RegisterAndClear() {
		BlockEvents.farmlandTrampled(event -> {
		});
		BlockEvents.Internal.clear();
	}

	@Test
	void modification_RegisterAndClear() {
		BlockEvents.modification(event -> {
		});
		BlockEvents.Internal.clear();
	}

	@Test
	void neighborNotify_RegisterAndClear() {
		BlockEvents.neighborNotify(event -> {
		});
		BlockEvents.Internal.clear();
	}

	@Test
	void internal_Clear_RemovesAll() {
		AtomicBoolean called = new AtomicBoolean(false);
		BlockEvents.broken(event -> called.set(true));
		BlockEvents.Internal.clear();
		BlockEvents.Internal.postBroken(null);
		assertFalse(called.get());
	}

	@Test
	void multipleBrokenCallbacks_AllRunAfterClearReAdd() {
		AtomicBoolean called = new AtomicBoolean(false);
		BlockEvents.broken(event -> called.set(true));
		BlockEvents.Internal.clear();
		BlockEvents.broken(event -> called.set(true));
		BlockEvents.Internal.postBroken(null);
		assertTrue(called.get());
	}
}
