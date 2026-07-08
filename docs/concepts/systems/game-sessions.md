# Game Sessions

Game sessions provide stateful, persistent game logic with automatic lifecycle management and data persistence — ideal for minigames, custom game modes, or anything that needs to maintain state across play sessions.

## Core Components

### GameSession

A `GameSession` holds an identifier, a `GameData` instance for persistence, a **public state** (defined by the game), and handlers for lifecycle events and ticks:

```java
GameSession session = new GameSession(
    Common.id("mymod", "mysession"),
    new MyGameData()
);

session.onStarting(() -> { /* setup */ });
session.onStopping(() -> { /* cleanup */ });
session.onCommonTick(level -> { /* runs 20x/sec on common */ });
session.onClientTick((minecraft, clientLevel) -> { /* client tick */ });
session.onServerTick((server, serverLevel) -> { /* server tick */ });
```

### GameState (Interface)

`GameState` is an interface that game authors implement to define their own state machines:

```java
public interface GameState {
    boolean isActive();     // Should this session receive ticks?
}
```

**Built-in `SimpleState`** provides common states out of the box:

| State      | `isActive()` |
|------------|--------------|
| `LOBBY`    | `true`       |
| `PLAYING`  | `true`       |
| `FINISHED` | `false`      |
| `STOPPED`  | `false`      |

**Custom states** for your game:

```java
enum MyGameState implements GameState {
    WARMUP(true), ROUND_ACTIVE(true), ROUND_OVER(false), MATCH_OVER(false);
    private final boolean active;
    MyGameState(boolean active) { this.active = active; }
    @Override public boolean isActive() { return active; }
}

// Use it:
session.publicState(MyGameState.WARMUP);
session.publicState(MyGameState.ROUND_ACTIVE);
```

### GameLifecycle

`GameLifecycle` is an enum that tracks the lifecycle state per **world** (save), not per session. The `GameManager` manages it:

```
STOPPED -> STARTING -> RUNNING -> STOPPING -> STOPPED
```

Check it via `manager.worldLifecycle(worldName)`. All sessions in a world share the world's lifecycle.

### GameData

Extend `GameData` to persist session state as NBT:

```groovy
class MyGameData extends GameData {
    MyGameData() {
        super(Common.id("mymod", "mysession"))
    }

    double getScore() { data().getDouble("score").orElse(0.0d) }
    void setScore(double v) { putDouble("score", v) }
    boolean getActive() { getBoolean("active") }
    void setActive(boolean v) { putBoolean("active", v) }
}
```

Supported types: `boolean`, `double`, `int`, `string`.

You can also register a one-time init callback:

```java
GameData data = new GameData(id)
    .onInit(() -> {
        data.putInt("lives", 3);
        data.putString("status", "new");
    });
```

The `onInit` callback fires once when the data file is first created or first loaded.

### GameManager

Access via `Common.getGameManager()`:

```groovy
def manager = Common.getGameManager()

manager.register("my_world", session)
manager.startSession("my_world", sessionId)
manager.stopSession("my_world", sessionId)
```

All operations are scoped per **world** (save). The world name is the save's level name.

#### Auto-start

Sessions with `autoStart(true)` are automatically started when the world loads:

```java
GameSession session = new GameSession(id, data)
        .autoStart(true)
        .publicState(SimpleState.LOBBY);
```

Call `manager.autoStartAll(worldName)` (called automatically in `FoundryEngineMod.onServerStarted`).

## Session Lifecycle

```
(world) STOPPED -> STARTING -> RUNNING -> STOPPING -> STOPPED
```

- **World start** (`autoStartAll`): Fire per-session `Starting` events -> load data -> run `onStarting` -> set world to `RUNNING` -> fire `Started` events
- **World stop** (`stopAll`): Fire per-session `Stopping` events -> run `onStopping` -> save data -> set world to `STOPPED` -> fire `Stopped` events
- **Individual stop** (`stopSession`): Stop a single session within a running world (fires events, saves data, but world stays running)

## Bundle Integration

```groovy
class MyBundle implements BundleEntrypoint {
    static final def SESSION_ID = Common.id("mymod", "main")
    static MyGameData DATA = new MyGameData()

    @Override
    void onLoad() {
        if (Common.getGameManager().hasSession("my_world", SESSION_ID)) return
        def session = new GameSession(SESSION_ID, DATA)
            .autoStart(true)
            .publicState(SimpleState.LOBBY)
        Common.getGameManager().register("my_world", session)
    }
}
```

Sessions with `autoStart(true)` will be started automatically when the world loads.

## API Reference

### GameSession

| Method                                                   | Description                                  |
|----------------------------------------------------------|----------------------------------------------|
| `id()`                                                   | Unique identifier for this session           |
| `data()`                                                 | Persistent `GameData` instance               |
| `publicState()`                                          | Current game-defined state (`GameState`)     |
| `publicState(GameState)`                                 | Set the game-defined state                   |
| `autoStart()` / `autoStart(boolean)`                     | Whether to start automatically on world load |
| `onStarting(Runnable)`                                   | Handler called before session starts         |
| `onStopping(Runnable)`                                   | Handler called before session stops          |
| `onInit(Runnable)`                                       | One-time handler called on first data load   |
| `onCommonTick(Consumer<Level>)`                          | Common tick handler (20x/sec)                |
| `onClientTick(BiConsumer<Minecraft, ClientLevel>)`       | Client tick handler                          |
| `onServerTick(BiConsumer<MinecraftServer, ServerLevel>)` | Server tick handler                          |
| `load()` / `save()`                                      | Manual data load/save                        |

### GameManager

| Method                              | Description                                            |
|-------------------------------------|--------------------------------------------------------|
| `register(session)`                 | Register a session for all worlds (current and future) |
| `register(worldName, session)`      | Register a session for a specific world                |
| `unregister(worldName, id)`         | Unregister a session                                   |
| `getSession(worldName, id)`         | Retrieve a session                                     |
| `hasSession(worldName, id)`         | Check if session exists                                |
| `getSessions(worldName)`            | All sessions for a world                               |
| `getAllSessions()`                  | All sessions across all worlds                         |
| `startSession(worldName, id)`       | Start a session                                        |
| `stopSession(worldName, id)`        | Stop a session                                         |
| `autoStartAll(worldName)`           | Start all auto-start sessions and set world to RUNNING |
| `stopAll(worldName)`                | Stop all sessions and set world to STOPPED             |
| `stopAll()`                         | Stop all sessions across all worlds                    |
| `worldLifecycle(worldName)`         | Current lifecycle for a world                          |
| `setWorldDataPath(worldName, path)` | Set per-world data directory                           |
| `worldDataPath(worldName)`          | Return per-world data directory                        |

### GameData

| Method                                        | Description                     |
|-----------------------------------------------|---------------------------------|
| `getBoolean` / `putBoolean`                   | Boolean values                  |
| `getDouble` / `putDouble`                     | Double values                   |
| `getInt` / `putInt`                           | Integer values                  |
| `getString` / `putString`                     | String values                   |
| `data()`                                      | Raw `CompoundTag` access        |
| `isInitialized()` / `setInitialized(boolean)` | Track first-time init           |
| `onInit(Runnable)`                            | Register one-time init callback |

### GameState / SimpleState

| Method                 | Description                        |
|------------------------|------------------------------------|
| `isActive()`           | Should this session receive ticks? |
| `SimpleState.LOBBY`    | Built-in: lobby/waiting            |
| `SimpleState.PLAYING`  | Built-in: actively playing         |
| `SimpleState.FINISHED` | Built-in: game over                |
| `SimpleState.STOPPED`  | Built-in: stopped                  |

### GameSession Events

All events are on the NeoForge event bus:

| Event | Cancellable | When |
|-------|-------------|------|
| `GameSessionEvent.Starting` | Yes | Before session start (can veto) |
| `GameSessionEvent.Started` | No | After session has started |
| `GameSessionEvent.Stopping` | Yes | Before session stop (can veto) |
| `GameSessionEvent.Stopped` | No | After session has stopped |

Each event has `getSessionId()` returning the session's `Identifier`.

## Example Usage

```groovy
// A simple timer session that tracks playtime
class TimerData extends GameData {
    TimerData() { super(Common.id("mymod", "timer")) }
    int getElapsed() { getInt("elapsed") }
    void setElapsed(int v) { putInt("elapsed", v) }
}

def worldName = "my_world"
def data = new TimerData()
def session = new GameSession(Common.id("mymod", "timer"), data)
    .autoStart(true)
    .publicState(SimpleState.PLAYING)
    .onCommonTick { level ->
        if (!level.isClientSide()) {
            data.setElapsed(data.getElapsed() + 1)
        }
    }
    .onStopping {
        println "Session ran for ${data.getElapsed()} ticks"
    }

Common.getGameManager().register(worldName, session)
```

## See Also

- [Stages](stages) — Progression framework
- [Bundles](../core/bundles) — Bundle lifecycle integration
- [Events](../core/events) — Game session events
