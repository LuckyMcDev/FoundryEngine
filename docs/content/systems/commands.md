# Commands Reference

All FoundryEngine commands are under `/engine`. Permission levels: **Any** (no perms needed), **Gamemaster**, **Admin** (OP).

## cutscene

| Command                                                        | Permission | What it does                           |
|----------------------------------------------------------------|------------|----------------------------------------|
| `list`                                                         | Gamemaster | List cutscenes in this dimension       |
| `add <name>`                                                   | Gamemaster | Create a cutscene at your position     |
| `remove <name>`                                                | Gamemaster | Remove a cutscene                      |
| `play <player> <name> [length] [easing] [holdStart] [holdEnd]` | Gamemaster | Play a cutscene                        |
| `cancel <player>`                                              | Gamemaster | Cancel a playing cutscene              |
| `linearize <name>`                                             | Gamemaster | Make a 2-node cutscene a straight line |
| `resetAll confirm`                                             | Gamemaster | Remove all cutscenes                   |

## waypoint

| Command                           | Permission | What it does                |
|-----------------------------------|------------|-----------------------------|
| `add <pos> <name> [icon] [color]` | Gamemaster | Add a waypoint              |
| `remove <pos>`                    | Gamemaster | Remove waypoint at position |
| `clear`                           | Gamemaster | Clear all waypoints         |
| `list`                            | Gamemaster | List all waypoints          |

## stage

| Command                    | Permission | What it does           |
|----------------------------|------------|------------------------|
| `<targets> add <stage>`    | Admin      | Add a stage to players |
| `<targets> remove <stage>` | Admin      | Remove a stage         |
| `<targets> clear`          | Admin      | Clear all stages       |
| `<targets> list`           | Any        | List player's stages   |

## screeneffect

```
/engine screeneffect <players> <effect> <intro> <hold> [outro] [easing] [command]
```

Effects: `black`, `circle`, `star`, `cinematic`

## reload

```
/engine reload
```

**Permission:** Admin. Reloads all bundles (calls `onUnload()`, then re-discovers everything).

## hand

```
/engine hand
```

**Permission:** Any. Shows info about the held item (registry name, NBT, components).

## eval

```
/engine eval <code>
```

**Permission:** Configurable. Run Groovy code inline:

```
/engine eval println "Hello!"
/engine eval Common.getGameStageHandler().addStage(player, "test")
```

## dump

| Subcommand        | Permission | What it does                    |
|-------------------|------------|---------------------------------|
| `all`             | Admin      | Dump all registries as markdown |
| `registry <name>` | Admin      | Dump a specific registry        |

## test

| Subcommand                             | Permission | What it does             |
|----------------------------------------|------------|--------------------------|
| `display block\|item\|text\|all\|kill` | Admin      | Test display entities    |
| `world open <name> <true\|false>`      | Admin      | Create a test world      |
| `world delete\|unload <name>`          | Admin      | Delete or unload a world |
| `fake spawn`                           | Admin      | Spawn a test mannequin   |

## Next

- [Cutscenes](cutscenes.md) — cutscene creation and playback
- [Stages](stages.md) — game stages system
- [Custom Worlds](instanced-worlds.md) — runtime dimensions
