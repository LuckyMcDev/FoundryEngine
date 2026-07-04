package de.luckymcdev.foundryengine.common.game;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    private static GameSession createSession(String name) {
        Identifier key = id(name);
        return new GameSession(key, new GameData(key));
    }

    @Test
    void register_NewSession_ReturnsTrue() {
        GameManager manager = new GameManager();
        GameSession session = createSession("test");
        assertTrue(manager.register(session));
    }

    @Test
    void register_Duplicate_ReturnsFalse() {
        GameManager manager = new GameManager();
        manager.register(createSession("dup"));
        assertFalse(manager.register(createSession("dup")));
    }

    @Test
    void getSession_Existing_ReturnsSession() {
        GameManager manager = new GameManager();
        GameSession session = createSession("my_session");
        manager.register(session);
        assertSame(session, manager.getSession(id("my_session")));
    }

    @Test
    void getSession_Missing_ReturnsNull() {
        GameManager manager = new GameManager();
        assertNull(manager.getSession(id("unknown")));
    }

    @Test
    void hasSession_Existing_True() {
        GameManager manager = new GameManager();
        manager.register(createSession("present"));
        assertTrue(manager.hasSession(id("present")));
    }

    @Test
    void hasSession_Missing_False() {
        GameManager manager = new GameManager();
        assertFalse(manager.hasSession(id("absent")));
    }

    @Test
    void getActiveSessions_Initially_Empty() {
        GameManager manager = new GameManager();
        assertTrue(manager.getActiveSessions().isEmpty());
    }

    @Test
    void getActiveSessions_AfterRegister_ContainsSession() {
        GameManager manager = new GameManager();
        GameSession session = createSession("s1");
        manager.register(session);
        assertEquals(1, manager.getActiveSessions().size());
        assertTrue(manager.getActiveSessions().contains(session));
    }

    @Test
    void unregister_Existing_Removes() {
        GameManager manager = new GameManager();
        manager.register(createSession("gone"));
        manager.unregister(id("gone"));
        assertNull(manager.getSession(id("gone")));
    }

    @Test
    void unregister_Missing_DoesNothing() {
        GameManager manager = new GameManager();
        assertDoesNotThrow(() -> manager.unregister(id("never_there")));
    }

    @Test
    void unregister_RunningSession_StopsIt() {
        GameManager manager = new GameManager();
        GameSession session = createSession("running");
        manager.register(session);
        session.state(GameState.RUNNING);
        manager.unregister(id("running"));
        assertNull(manager.getSession(id("running")));
    }

    @Test
    void startSession_Unknown_ReturnsFalse() {
        GameManager manager = new GameManager();
        assertFalse(manager.startSession(id("unknown")));
    }

    @Test
    void startSession_AlreadyRunning_ReturnsFalse() {
        GameManager manager = new GameManager();
        GameSession session = createSession("running");
        manager.register(session);
        session.state(GameState.RUNNING);
        // Won't start because it's already RUNNING
        assertFalse(manager.startSession(id("running")));
    }

    @Test
    void stopSession_Unknown_ReturnsFalse() {
        GameManager manager = new GameManager();
        assertFalse(manager.stopSession(id("unknown")));
    }

    @Test
    void stopSession_Stopped_ReturnsFalse() {
        GameManager manager = new GameManager();
        GameSession session = createSession("stopped");
        manager.register(session);
        assertFalse(manager.stopSession(id("stopped")));
    }

    @Test
    void stopAll_NoSessions_DoesNothing() {
        GameManager manager = new GameManager();
        assertDoesNotThrow(manager::stopAll);
    }

    @Test
    void getActiveSessions_Unmodifiable() {
        GameManager manager = new GameManager();
        assertThrows(UnsupportedOperationException.class,
                () -> manager.getActiveSessions().add(createSession("x")));
    }

    @Test
    void tickCommon_RunningSessions_Ticked() {
        GameManager manager = new GameManager();
        GameSession session = createSession("s");
        AtomicBoolean ticked = new AtomicBoolean(false);
        session.onCommonTick(l -> ticked.set(true));
        session.state(GameState.RUNNING);
        manager.register(session);
        manager.tickCommon(null);
        assertTrue(ticked.get());
    }

    @Test
    void tickCommon_NonRunningSessions_NotTicked() {
        GameManager manager = new GameManager();
        GameSession session = createSession("s");
        AtomicBoolean ticked = new AtomicBoolean(false);
        session.onCommonTick(l -> ticked.set(true));
        session.state(GameState.STOPPED);
        manager.register(session);
        manager.tickCommon(null);
        assertFalse(ticked.get());
    }

    @Test
    void onBundlePreUnload_StopsBundleSessions() {
        GameManager manager = new GameManager();
        GameSession session = createSession("test_s");
        session.state(GameState.RUNNING);
        manager.register(session);

        // Simulate unloading a bundle with namespace "test"
        var bundle = new FakeBundle("test");
        manager.onBundlePreUnload(bundle);
        // Session state is now STOPPED (was stopped by onBundlePreUnload)
        assertEquals(GameState.STOPPED, session.state());
    }

    @Test
    void onBundleReloadStarted_StopsAllAndClears() {
        GameManager manager = new GameManager();
        manager.register(createSession("a"));
        manager.register(createSession("b"));
        manager.onBundleReloadStarted();
        assertTrue(manager.getActiveSessions().isEmpty());
    }

    private static class FakeBundle extends de.luckymcdev.foundryengine.common.bundle.Bundle {
        private final String bundleId;
        public FakeBundle(String id) {
            super(null, null, null, null, null, null);
            this.bundleId = id;
        }
        @Override
        public de.luckymcdev.foundryengine.common.bundle.info.BundleInfo info() {
            return new de.luckymcdev.foundryengine.common.bundle.info.BundleInfo(
                    bundleId, bundleId, java.util.List.of(), new de.luckymcdev.foundryengine.common.bundle.info.BundleInfo.VersionInfo(1, 0, 0), java.util.List.of());
        }
    }
}
