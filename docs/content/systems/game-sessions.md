# Game Sessions

Game sessions provide a managed lifecycle for minigames, custom game modes, or anything that needs persistent state across play sessions.

## Core idea

A **GameSession** is a named container with:

- **State** — what phase the game is in (lobby, playing, finished)
- **Data** — persistent information saved as NBT
- **Lifecycle** — automatic start/stop with the world

## Creating a session

```groovy
import de.luckymcdev.foundryengine.common.game.session.GameSession
import de.luckymcdev.foundryengine.common.game.session.GameData
import de.luckymcdev.foundryengine.common.game.session.SimpleState
import de.luckymcdev.foundryengine.common.Common

// Define your data structure
class MyGameData extends GameData {
    MyGameData() { super(Common.id("mymod", "mysession")) }
    int getScore() { getInt("score") }
    void setScore(int v) { putInt("score", v) }
}

// Create the session
def data = new MyGameData()
def session = new GameSession(Common.id("mymod", "mysession"), data)
    .autoStart(true)                              // Start when world loads
    .publicState(SimpleState.LOBBY)               // Initial state
    .onCommonTick { level ->                      // Runs 20x/sec
        if (!level.isClientSide()) {
            data.setScore(data.getScore() + 1)
        }
    }
    .onStopping {
        println "Session ran for ${data.getScore()} ticks"
    }

Common.getGameManager().register("my_world", session)
```

## Built-in states

| State                  | What it means       | Receives ticks? |
|------------------------|---------------------|-----------------|
| `SimpleState.LOBBY`    | Waiting for players | Yes             |
| `SimpleState.PLAYING`  | Game is active      | Yes             |
| `SimpleState.FINISHED` | Game is over        | No              |
| `SimpleState.STOPPED`  | Session stopped     | No              |

## Custom states

```groovy
enum MyGameState implements GameState {
    WARMUP(true), ROUND_ACTIVE(true), ROUND_OVER(false), MATCH_OVER(false);
    private final boolean active
    MyGameState(boolean active) { this.active = active }
    @Override
    boolean isActive() { return active; }
}

session.publicState(MyGameState.WARMUP
```

## Managing sessions

```groovy
def manager = Common.getGameManager()

manager.register("my_world", session)
manager.startSession("my_world", sessionId)
manager.stopSession("my_world", sessionId)
manager.stopAll("my_world")        // Stop all sessions in a world
```

## Session lifecycle

```
STOPPED → STARTING → RUNNING → STOPPING → STOPPED
```

- **World loads**: auto-start sessions fire `onStarting`, load data, switch to RUNNING
- **World saves**: sessions fire `onStopping`, save data, switch to STOPPED

## Session events

```groovy
GameEvents.onStarted { event ->
    println "Session ${event.sessionId} started"
}
```

## Next

- [Stages](stages.md) — progression framework
- [Custom Worlds](instanced-worlds.md) — runtime dimensions for sessions
