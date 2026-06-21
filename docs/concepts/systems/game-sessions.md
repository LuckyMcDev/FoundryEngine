# Game Sessions

Game sessions provide stateful, persistent game logic with automatic lifecycle management and data persistence — ideal for minigames, custom game modes, or anything that needs to maintain state across play sessions.

## Core Components

### GameSession

A `GameSession` holds an identifier, a `GameData` instance for persistence, and a state machine with configurable lifecycle hooks:

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

### GameManager

Access via `Common.getGameManager()`:

```groovy
def manager = Common.getGameManager()

manager.register(session)
manager.startSession(sessionId)
manager.isSessionRunning(sessionId)
manager.stopSession(sessionId)
```

## Session Lifecycle

```
STOPPED -> STARTING -> RUNNING -> STOPPING -> STOPPED
```

- **On start**: Fire `Starting` event -> load data from disk -> run `onStarting` -> `RUNNING`
- **On stop**: Fire `Stopping` event -> run `onStopping` -> save data to disk -> `STOPPED`

## Bundle Integration

Sessions integrate with the bundle lifecycle — they are automatically stopped on bundle reload:

```groovy
class MyBundle implements BundleEntrypoint {
    static final def SESSION_ID = Common.id("mymod", "main")
    static MyGameData DATA = new MyGameData()
    static GameSession SESSION = new GameSession(SESSION_ID, DATA)

    @Override
    void onLoad() {
        if (Common.getGameManager().hasSession(SESSION_ID)) return
        Common.getGameManager().register(SESSION)
        Common.getGameManager().startSession(SESSION_ID)
    }

    @Override
    void onUnload() {
        Common.getGameManager().stopSession(SESSION_ID)
    }
}
```

## Data Persistence

Session data is saved as NBT to:

```
<game-directory>/FoundryEngine/.cache/game/<namespace>/<session-name>.nbt
```

## API Reference

### GameSession

| Method | Description |
|--------|-------------|
| `isRunning()` | Check if session is active |
| `onStarting(Runnable)` | Handler called before session starts |
| `onStopping(Runnable)` | Handler called before session stops |
| `onCommonTick(Consumer<Level>)` | Common tick handler (20x/sec) |
| `onClientTick(BiConsumer<Minecraft, ClientLevel>)` | Client tick handler |
| `onServerTick(BiConsumer<MinecraftServer, ServerLevel>)` | Server tick handler |
| `load()` / `save()` | Manual data load/save |

### GameManager

| Method | Description |
|--------|-------------|
| `register(session)` | Register a session |
| `startSession(id)` / `stopSession(id)` | Start or stop a session |
| `hasSession(id)` | Check if session is registered |
| `getSession(id)` | Retrieve a session by ID |
| `isSessionRunning(id)` | Check running state |
| `stopAll()` | Stop all sessions |

### GameData

| Method | Description |
|--------|-------------|
| `getBoolean` / `putBoolean` | Boolean values |
| `getDouble` / `putDouble` | Double values |
| `getInt` / `putInt` | Integer values |
| `getString` / `putString` | String values |
| `data()` | Raw `CompoundTag` access |

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

def data = new TimerData()
def session = new GameSession(Common.id("mymod", "timer"), data)
session.onCommonTick { level ->
    if (!level.isClientSide()) {
        data.setElapsed(data.getElapsed() + 1)
    }
}
session.onStopping {
    println "Session ran for ${data.getElapsed()} ticks"
}

Common.getGameManager().register(session)
Common.getGameManager().startSession(Common.id("mymod", "timer"))
```

## See Also

- [Stages](stages) — Progression framework
- [Bundles](../core/bundles) — Bundle lifecycle integration
- [Events](../core/events) — Game session events
