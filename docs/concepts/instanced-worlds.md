# Instanced Worlds

FoundryEngine lets you create runtime dimensions on-the-fly — temporary or persistent — with full control over chunk generation, game rules, clock behavior, and difficulty.

## Overview

The `EngineLevels` API provides a singleton for creating and managing runtime dimensions:

```groovy
import de.luckymcdev.foundryengine.common.world.level.EngineLevels
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelConfig
import de.luckymcdev.foundryengine.common.world.level.util.VoidChunkGenerator

def server = MinecraftServer.getServer()
def config = new RuntimeLevelConfig()
    .setGenerator(new VoidChunkGenerator(server, "minecraft:the_end"))
    .setShouldTickTime(true)
    .setGameRule(GameRules.DO_MOB_SPAWNING, false)

// Open a temporary level (deleted on server stop)
def handle = EngineLevels.get(server).openTemporaryLevel(
    Common.id("my_dungeon"), config)

// Or persistent (survives restarts)
def persistent = EngineLevels.get(server).getOrOpenPersistentLevel(
    Common.id("my_world"), config)

// Teleport a player
player.teleport(new TeleportTransition(
    handle.asLevel(), new Vec3(0, 100, 0), Vec3.ZERO, 0, 0,
    TeleportTransition.DO_NOTHING))
```

## Temporary vs Persistent

| Style | Behavior |
|-------|----------|
| `TEMPORARY` | Created at runtime, deleted on server stop. Perfect for dungeons, minigames, or instanced player areas. |
| `PERSISTENT` | Persists across restarts. All blocks, entities, and data are saved. Must be re-opened manually each startup. |

## RuntimeLevelConfig

The config builder gives you full control:

| Method | Description |
|--------|-------------|
| `setGenerator(ChunkGenerator)` | Set the chunk generator (void, overworld copy, custom) |
| `setDimensionType(Holder<DimensionType>)` | Dimension type (renderer, lighting, etc.) |
| `setSeed(long)` | Custom world seed |
| `setGameTime(long)` | Starting game time |
| `setDifficulty(Difficulty)` | Override difficulty |
| `setGameRule(GameRule<T>, T)` | Set individual game rules |
| `setShouldTickTime(boolean)` | Enable/disable time advancement |
| `setMirrorOverworldGameRules(boolean)` | Copy overworld game rules |
| `setMirrorOverworldDifficulty(boolean)` | Copy overworld difficulty |
| `setMirrorOverworldClocks(boolean)` | Copy overworld clock settings |
| `setFlat(boolean)` | Enable flat world lighting |
| `setLevelConstructor(RuntimeLevel.Constructor)` | Custom level factory |

## Chunk Generators

### VoidChunkGenerator

An empty void world. Good for arenas, boss rooms, and sky-based levels:

```groovy
import de.luckymcdev.foundryengine.common.world.level.util.VoidChunkGenerator

// From biome registry
new VoidChunkGenerator(server, "minecraft:plains")
// From biome key
new VoidChunkGenerator(server, ResourceKey.create(
    Registries.BIOME, ResourceLocation.parse("minecraft:plains")))
```

### TransientChunkGenerator

Abstract base for generators that cannot be serialized. Extend this for completely custom generation.

### Copying the Overworld Generator

```groovy
config.setGenerator(server.overworld().getChunkSource().getGenerator())
```

## Clock Control

Runtime dimensions can have custom clock mechanics. Disable normal time, control it programmatically, or synchronize with another dimension:

```groovy
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeClockManager

// Custom clock that never advances
config.setShouldTickTime(false)

// Access clock manager for advanced control
def clockManager = new RuntimeClockManager(() -> false) // never advance
config.setClockManagerConstructor(clockManager)
```

## Deleting and Unloading

```groovy
// Permanent deletion (removes all saved data)
handle.delete()

// Unload only (saves if persistent, discards if temporary)
handle.unload()
```

## Use Cases

- **Dungeons**: Temporary instanced dungeons per player/party
- **Minigames**: Self-contained arenas with custom rules
- **Hub worlds**: Persistent lobby dimensions with custom generators
- **Skyblock-style**: Void worlds with custom island generation
- **Testing**: Isolated dimensions for development

## See Also

- [Commands](commands) — `/engine test world` for testing
- [Areas](areas) — Trigger dimension travel with spatial zones
- [Stages](stages) — Gate dimension access with progression
