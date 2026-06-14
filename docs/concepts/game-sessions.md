# Game Sessions

Game sessions provide stateful, persistent game logic with automatic lifecycle management and data persistence — ideal
for minigames, custom game modes, or anything that needs to maintain state across play sessions.

## Core Components

### GameSession

```java
GameSession session = new GameSession(
    Common.id("mymod", "mysession"),
    new MyGameData()
);

session.onStarting(() -> { /* setup */ });
session.onStopping(() -> { /* cleanup */ });
session.onCommonTick(level -> { /* 20x/sec */ });
session.onClientTick((minecraft, clientLevel) -> { /* client tick */ });
session.onServerTick((server, serverLevel) -> { /* server tick */ });
```

### GameData

Extend `GameData` to persist session state:

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

### GameManager

```groovy
def manager = Common.getGameManager()

manager.register(session)
manager.startSession(sessionId)

manager.isSessionRunning(sessionId)
manager.stopSession(sessionId)
```

## Bundle Integration

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

## Session Lifecycle

```
STOPPED → STARTING → RUNNING → STOPPING → STOPPED
```

**On start:** fire `Starting` event → load data from disk → run `onStarting` → `RUNNING`

**On stop:** fire `Stopping` event → run `onStopping` → save data to disk → `STOPPED`

## Data Persistence

Session data is saved as NBT to:

```
<game-directory>/FoundryEngine/.cache/game/<namespace>/<session-name>.nbt
```

Supported types: `boolean`, `double`, `int`, `string`.

## API Reference

### GameSession

| Method                                                   | Description                          |
|----------------------------------------------------------|--------------------------------------|
| `isRunning()`                                            | Check if session is active           |
| `onStarting(Runnable)`                                   | Handler called before session starts |
| `onStopping(Runnable)`                                   | Handler called before session stops  |
| `onCommonTick(Consumer<Level>)`                          | Common tick handler                  |
| `onClientTick(BiConsumer<Minecraft, ClientLevel>)`       | Client tick handler                  |
| `onServerTick(BiConsumer<MinecraftServer, ServerLevel>)` | Server tick handler                  |
| `load()` / `save()`                                      | Manual data load/save                |

### GameManager

| Method                                 | Description                    |
|----------------------------------------|--------------------------------|
| `register(session)`                    | Register a session             |
| `startSession(id)` / `stopSession(id)` | Start or stop a session        |
| `hasSession(id)`                       | Check if session is registered |
| `getSession(id)`                       | Retrieve a session by ID       |
| `isSessionRunning(id)`                 | Check running state            |
| `stopAll()`                            | Stop all sessions              |

### GameData

| Method                      | Description              |
|-----------------------------|--------------------------|
| `getBoolean` / `putBoolean` | Boolean values           |
| `getDouble` / `putDouble`   | Double values            |
| `getInt` / `putInt`         | Integer values           |
| `getString` / `putString`   | String values            |
| `data()`                    | Raw `CompoundTag` access |