# Custom Worlds

FoundryEngine lets you create dimensions at runtime with custom rules, chunk generators, and clock behavior.

## Two types of custom worlds

| Type           | Behavior                                   | Use case                     |
|----------------|--------------------------------------------|------------------------------|
| **Temporary**  | Created at runtime, deleted on server stop | Dungeons, arenas, minigames  |
| **Persistent** | Survives restarts, saves all data          | Hub worlds, persistent zones |

## Creating a temporary world

```groovy
import de.luckymcdev.foundryengine.common.world.level.EngineLevels
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelConfig
import de.luckymcdev.foundryengine.common.world.level.util.VoidChunkGenerator

def server = MinecraftServer.getServer()
def config = new RuntimeLevelConfig()
        .setGenerator(new VoidChunkGenerator(server, "minecraft:plains"))
        .setShouldTickTime(true)
        .setGameRule(GameRules.DO_MOB_SPAWNING, false)

def handle = EngineLevels.get(server).openTemporaryLevel(
        Common.id("my_dungeon"), config)

// Teleport a player there
player.teleport(new TeleportTransition(
        handle.asLevel(), new Vec3(0, 100, 0), Vec3.ZERO, 0, 0,
        TeleportTransition.DO_NOTHING))
```

## Creating a persistent world

```groovy
def persistent = EngineLevels.get(server).getOrOpenPersistentLevel(
        Common.id("my_world"), config)
```

## World configuration

The `RuntimeLevelConfig` lets you control everything:

| Method                         | What it does                                       |
|--------------------------------|----------------------------------------------------|
| `setGenerator(ChunkGenerator)` | Set chunk generator (void, overworld copy, custom) |
| `setSeed(long)`                | Custom world seed                                  |
| `setDifficulty(Difficulty)`    | Override difficulty                                |
| `setGameRule(GameRule, value)` | Set individual game rules                          |
| `setShouldTickTime(boolean)`   | Enable/disable time                                |
| `setFlat(boolean)`             | Flat world lighting                                |

## Chunk generators

### VoidChunkGenerator

An empty void — good for arenas, boss rooms, sky-based levels:

```groovy
new VoidChunkGenerator(server, "minecraft:plains")
```

### Copy the overworld generator

```groovy
config.setGenerator(server.overworld().getChunkSource().getGenerator())
```

## Deleting worlds

```groovy
// Permanently delete (removes all data)
handle.delete()

// Unload only (saves if persistent)
handle.unload()
```

## Use cases

- **Dungeons**: Temporary instanced dungeons per player
- **Minigames**: Self-contained arenas with custom rules
- **Hub worlds**: Persistent lobby dimensions
- **Skyblock**: Void worlds with custom generators
- **Testing**: Isolated dimensions for development

## Next

- [Game Stages](stages.md) — gate dimension access with progression
- [Areas](areas.md) — trigger dimension travel with zones
