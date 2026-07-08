package de.luckymcdev.foundryengine.common.game;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

	private static final String WORLD = "test_world";

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}

	private static GameSession createSession(String name) {
		Identifier key = id(name);
		return new GameSession(key, new GameData(key));
	}

	@Test
	void registerGlobal_NewSession_ReturnsTrue() {
		GameManager manager = new GameManager();
		assertTrue(manager.register(createSession("g")));
	}

	@Test
	void registerGlobal_Duplicate_ReturnsFalse() {
		GameManager manager = new GameManager();
		manager.register(createSession("g"));
		assertFalse(manager.register(createSession("g")));
	}

	@Test
	void registerGlobal_AppearsInAllWorlds() {
		GameManager manager = new GameManager();
		GameSession session = createSession("g");
		manager.register(session);
		assertSame(session, manager.getSession("any_world", id("g")));
		assertTrue(manager.hasSession("any_world", id("g")));
	}

	@Test
	void registerGlobal_SeedsNewWorld() {
		GameManager manager = new GameManager();
		GameSession session = createSession("g");
		manager.register(session);
		manager.register("new_world", createSession("local"));
		assertSame(session, manager.getSession("new_world", id("g")));
	}

	@Test
	void registerGlobal_AddsToExistingWorlds() {
		GameManager manager = new GameManager();
		manager.register("existing", createSession("local"));
		GameSession session = createSession("g");
		manager.register(session);
		assertSame(session, manager.getSession("existing", id("g")));
	}

	@Test
	void register_NewSession_ReturnsTrue() {
		GameManager manager = new GameManager();
		GameSession session = createSession("test");
		assertTrue(manager.register(WORLD, session));
	}

	@Test
	void register_Duplicate_ReturnsFalse() {
		GameManager manager = new GameManager();
		manager.register(WORLD, createSession("dup"));
		assertFalse(manager.register(WORLD, createSession("dup")));
	}

	@Test
	void register_SameIdDifferentWorld_Succeeds() {
		GameManager manager = new GameManager();
		assertTrue(manager.register("world_a", createSession("shared")));
		assertTrue(manager.register("world_b", createSession("shared")));
	}

	@Test
	void getSession_Existing_ReturnsSession() {
		GameManager manager = new GameManager();
		GameSession session = createSession("my_session");
		manager.register(WORLD, session);
		assertSame(session, manager.getSession(WORLD, id("my_session")));
	}

	@Test
	void getSession_Missing_ReturnsNull() {
		GameManager manager = new GameManager();
		assertNull(manager.getSession(WORLD, id("unknown")));
	}

	@Test
	void getSession_DifferentWorld_ReturnsNull() {
		GameManager manager = new GameManager();
		manager.register("world_a", createSession("s"));
		assertNull(manager.getSession("world_b", id("s")));
	}

	@Test
	void hasSession_Existing_True() {
		GameManager manager = new GameManager();
		manager.register(WORLD, createSession("present"));
		assertTrue(manager.hasSession(WORLD, id("present")));
	}

	@Test
	void hasSession_Missing_False() {
		GameManager manager = new GameManager();
		assertFalse(manager.hasSession(WORLD, id("absent")));
	}

	@Test
	void getSessions_Initially_Empty() {
		GameManager manager = new GameManager();
		assertTrue(manager.getSessions(WORLD).isEmpty());
	}

	@Test
	void getSessions_AfterRegister_ContainsSession() {
		GameManager manager = new GameManager();
		GameSession session = createSession("s1");
		manager.register(WORLD, session);
		assertEquals(1, manager.getSessions(WORLD).size());
		assertTrue(manager.getSessions(WORLD).contains(session));
	}

	@Test
	void getAllSessions_AcrossWorlds() {
		GameManager manager = new GameManager();
		manager.register("world_a", createSession("a"));
		manager.register("world_b", createSession("b"));
		assertEquals(2, manager.getAllSessions().size());
	}

	@Test
	void unregister_Existing_Removes() {
		GameManager manager = new GameManager();
		manager.register(WORLD, createSession("gone"));
		manager.unregister(WORLD, id("gone"));
		assertNull(manager.getSession(WORLD, id("gone")));
	}

	@Test
	void unregister_Missing_DoesNothing() {
		GameManager manager = new GameManager();
		assertDoesNotThrow(() -> manager.unregister(WORLD, id("never_there")));
	}

	@Test
	void unregister_StartedSession_StopsIt() {
		GameManager manager = new GameManager();
		GameSession session = createSession("running");
		manager.register(WORLD, session);
		session.started = true;
		manager.unregister(WORLD, id("running"));
		assertNull(manager.getSession(WORLD, id("running")));
	}

	@Test
	void startSession_Unknown_ReturnsFalse() {
		GameManager manager = new GameManager();
		assertFalse(manager.startSession(WORLD, id("unknown")));
	}

	@Test
	void startSession_AlreadyStarted_ReturnsFalse() {
		GameManager manager = new GameManager();
		GameSession session = createSession("running");
		manager.register(WORLD, session);
		session.started = true;
		assertFalse(manager.startSession(WORLD, id("running")));
	}

	@Test
	void stopSession_Unknown_ReturnsFalse() {
		GameManager manager = new GameManager();
		assertFalse(manager.stopSession(WORLD, id("unknown")));
	}

	@Test
	void stopSession_NotStarted_ReturnsFalse() {
		GameManager manager = new GameManager();
		GameSession session = createSession("stopped");
		manager.register(WORLD, session);
		assertFalse(manager.stopSession(WORLD, id("stopped")));
	}

	@Test
	void stopAll_NoSessions_DoesNothing() {
		GameManager manager = new GameManager();
		assertDoesNotThrow(() -> manager.stopAll(WORLD));
	}

	@Test
	void stopAll_Global_NoSessions_DoesNothing() {
		GameManager manager = new GameManager();
		assertDoesNotThrow(() -> manager.stopAll());
	}

	@Test
	void getSessions_Unmodifiable() {
		GameManager manager = new GameManager();
		assertThrows(UnsupportedOperationException.class,
			() -> manager.getSessions(WORLD).add(createSession("x")));
	}

	@Test
	void tickCommon_RunningSessions_Ticked() {
		GameManager manager = new GameManager();
		GameSession session = createSession("s");
		AtomicBoolean ticked = new AtomicBoolean(false);
		session.onCommonTick(l -> ticked.set(true));
		session.started = true;
		session.publicState(SimpleState.PLAYING);
		manager.register("unknown", session);
		manager.autoStartAll("unknown");
		manager.tickCommon(null);
		assertTrue(ticked.get());
	}

	@Test
	void tickCommon_NonStartedSessions_NotTicked() {
		GameManager manager = new GameManager();
		GameSession session = createSession("s");
		AtomicBoolean ticked = new AtomicBoolean(false);
		session.onCommonTick(l -> ticked.set(true));
		session.publicState(SimpleState.PLAYING);
		manager.register("unknown", session);
		manager.tickCommon(null);
		assertFalse(ticked.get());
	}

	@Test
	void autoStartAll_StartsAutoStartSessions() {
		GameManager manager = new GameManager();
		GameSession session = createSession("s");
		session.autoStart(true);
		manager.register(WORLD, session);
		manager.autoStartAll(WORLD);
		assertTrue(session.started);
		assertEquals(GameLifecycle.RUNNING, manager.worldLifecycle(WORLD));
	}

	@Test
	void autoStartAll_DoesNotStartNonAutoSessions() {
		GameManager manager = new GameManager();
		GameSession session = createSession("s");
		manager.register(WORLD, session);
		manager.autoStartAll(WORLD);
		assertFalse(session.started);
	}

	@Test
	void autoStartAll_AlreadyRunning_DoesNothing() {
		GameManager manager = new GameManager();
		GameSession session = createSession("s");
		manager.register(WORLD, session);
		manager.autoStartAll(WORLD);
		manager.autoStartAll(WORLD);
		assertEquals(GameLifecycle.RUNNING, manager.worldLifecycle(WORLD));
	}

	@Test
	void worldLifecycle_Default_Stopped() {
		GameManager manager = new GameManager();
		assertEquals(GameLifecycle.STOPPED, manager.worldLifecycle("unknown_world"));
	}

	@Test
	void worldLifecycle_AfterAutoStart_Running() {
		GameManager manager = new GameManager();
		manager.register(WORLD, createSession("s"));
		manager.autoStartAll(WORLD);
		assertEquals(GameLifecycle.RUNNING, manager.worldLifecycle(WORLD));
	}

	@Test
	void worldLifecycle_AfterStopAll_Stopped() {
		GameManager manager = new GameManager();
		manager.register(WORLD, createSession("s"));
		manager.autoStartAll(WORLD);
		manager.stopAll(WORLD);
		assertEquals(GameLifecycle.STOPPED, manager.worldLifecycle(WORLD));
	}

	@Test
	void onBundlePreUnload_StopsBundleSessions() {
		GameManager manager = new GameManager();
		GameSession session = createSession("test_s");
		session.started = true;
		manager.register(WORLD, session);
		manager.autoStartAll(WORLD);

		var bundle = new FakeBundle("test");
		manager.onBundlePreUnload(bundle);
		assertFalse(session.started);
	}

	@Test
	void onBundleReloadStarted_StopsAllAndClears() {
		GameManager manager = new GameManager();
		manager.register(WORLD, createSession("a"));
		manager.register(WORLD, createSession("b"));
		manager.onBundleReloadStarted();
		assertTrue(manager.getSessions(WORLD).isEmpty());
		assertEquals(GameLifecycle.STOPPED, manager.worldLifecycle(WORLD));
	}

	@Test
	void worldDataPath_Default_ReturnsCommonGame() {
		GameManager manager = new GameManager();
		assertEquals(de.luckymcdev.foundryengine.common.Common.GAME, manager.worldDataPath("unknown"));
	}

	@Test
	void worldDataPath_Set_ReturnsCustom() {
		GameManager manager = new GameManager();
		java.nio.file.Path custom = java.nio.file.Path.of("custom", "path");
		manager.setWorldDataPath(WORLD, custom);
		assertEquals(custom, manager.worldDataPath(WORLD));
	}

	private static class FakeBundle extends de.luckymcdev.foundryengine.common.bundle.Bundle {
		private final String bundleId;

		public FakeBundle(String id) {
			super(null, null, null, null, null);
			this.bundleId = id;
		}

		@Override
		public de.luckymcdev.foundryengine.common.bundle.info.BundleInfo info() {
			return new de.luckymcdev.foundryengine.common.bundle.info.BundleInfo(
				bundleId, bundleId, java.util.List.of(), new de.luckymcdev.foundryengine.common.bundle.info.BundleInfo.VersionInfo(1, 0, 0), java.util.List.of());
		}
	}
}
