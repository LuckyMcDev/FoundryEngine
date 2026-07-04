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
        assertSame(GameState.STOPPED, session.state());
    }

    @Test
    void state_Initially_Stopped() {
        GameSession session = new GameSession(id("s"), new GameData(id("s")));
        assertEquals(GameState.STOPPED, session.state());
        assertFalse(session.isRunning());
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
    void fluentApi_ReturnsThis() {
        GameSession session = new GameSession(id("s"), new GameData(id("s")));
        assertSame(session, session.onCommonTick(l -> {}));
        assertSame(session, session.onClientTick((m, l) -> {}));
        assertSame(session, session.onServerTick((s, l) -> {}));
        assertSame(session, session.onStarting(() -> {}));
        assertSame(session, session.onStopping(() -> {}));
    }

    @Test
    void isRunning_RunningState_True() {
        GameSession session = new GameSession(id("s"), new GameData(id("s")));
        session.state(GameState.RUNNING);
        assertTrue(session.isRunning());
    }

    @Test
    void state_Transitions() {
        GameSession session = new GameSession(id("s"), new GameData(id("s")));
        assertSame(GameState.STOPPED, session.state());
        session.state(GameState.STARTING);
        assertSame(GameState.STARTING, session.state());
        session.state(GameState.RUNNING);
        assertSame(GameState.RUNNING, session.state());
        session.state(GameState.STOPPING);
        assertSame(GameState.STOPPING, session.state());
        session.state(GameState.STOPPED);
        assertSame(GameState.STOPPED, session.state());
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
