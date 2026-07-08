package de.luckymcdev.foundryengine.common.game;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}

	@Test
	void constructor_SetsIdAndData() {
		Identifier key = id("test_session");
		GameData data = new GameData(key);
		GameSession session = new GameSession(key, data);
		assertEquals(key, session.id());
		assertSame(data, session.data());
	}

	@Test
	void started_Default_False() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		assertFalse(session.started);
	}

	@Test
	void publicState_Default_Lobby() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		assertSame(SimpleState.LOBBY, session.publicState());
	}

	@Test
	void publicState_CanBeSet() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		session.publicState(SimpleState.PLAYING);
		assertSame(SimpleState.PLAYING, session.publicState());
	}

	@Test
	void autoStart_Default_False() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		assertFalse(session.autoStart());
	}

	@Test
	void autoStart_CanBeEnabled() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		session.autoStart(true);
		assertTrue(session.autoStart());
	}

	@Test
	void onCommonTick_HandlerCalled() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		AtomicBoolean ticked = new AtomicBoolean(false);
		session.onCommonTick(level -> ticked.set(true));
		session.onCommonTick((net.minecraft.world.level.Level) null);
		assertTrue(ticked.get());
	}

	@Test
	void onClientTick_HandlerCalled() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		AtomicBoolean ticked = new AtomicBoolean(false);
		session.onClientTick((mc, level) -> ticked.set(true));
		session.onClientTick(null, null);
		assertTrue(ticked.get());
	}

	@Test
	void onServerTick_HandlerCalled() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		AtomicBoolean ticked = new AtomicBoolean(false);
		session.onServerTick((server, level) -> ticked.set(true));
		session.onServerTick(null, null);
		assertTrue(ticked.get());
	}

	@Test
	void onStarting_HandlerCalled() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		AtomicBoolean started = new AtomicBoolean(false);
		session.onStarting(() -> started.set(true));
		session.onStarting();
		assertTrue(started.get());
	}

	@Test
	void onStopping_HandlerCalled() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		AtomicBoolean stopped = new AtomicBoolean(false);
		session.onStopping(() -> stopped.set(true));
		session.onStopping();
		assertTrue(stopped.get());
	}

	@Test
	void onInit_HandlerCalled() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		AtomicBoolean initialized = new AtomicBoolean(false);
		session.onInit(() -> initialized.set(true));
		session.onInit();
		assertTrue(initialized.get());
	}

	@Test
	void fluentApi_ReturnsThis() {
		GameSession session = new GameSession(id("s"), new GameData(id("s")));
		assertSame(session, session.onCommonTick(l -> {
		}));
		assertSame(session, session.onClientTick((m, l) -> {
		}));
		assertSame(session, session.onServerTick((s, l) -> {
		}));
		assertSame(session, session.onStarting(() -> {
		}));
		assertSame(session, session.onStopping(() -> {
		}));
		assertSame(session, session.onInit(() -> {
		}));
		assertSame(session, session.autoStart(true));
		assertSame(session, session.publicState(SimpleState.PLAYING));
	}

	@Test
	void data_ReadWrite() {
		GameData data = new GameData(id("test_data"));
		data.putString("name", "world");
		data.putInt("value", 42);
		data.putBoolean("flag", true);
		data.putDouble("pi", 3.14);

		assertEquals("world", data.getString("name"));
		assertEquals(42, data.getInt("value"));
		assertTrue(data.getBoolean("flag"));
		assertEquals(3.14, data.getDouble("pi"), 0.001);
		assertEquals(id("test_data"), data.identifier());
	}

	@Test
	void data_MissingKeys_DefaultValues() {
		GameData data = new GameData(id("d"));
		assertNull(data.getString("missing"));
		assertEquals(0, data.getInt("missing"));
		assertFalse(data.getBoolean("missing"));
		assertEquals(0.0, data.getDouble("missing"), 0.001);
	}

	@Test
	void data_DataCompound_GetSet() {
		GameData data = new GameData(id("d"));
		assertNotNull(data.data());
		data.data(data.data());
		assertNotNull(data.data());
	}
}
