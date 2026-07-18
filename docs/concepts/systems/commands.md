# Commands reference

All FoundryEngine commands are registered under `/engine` and require specific permission levels.

## Permission levels

| Level      | Name | Description                         |
|------------|------|-------------------------------------|
| Any        | -    | No special permission required      |
| Gamemaster | -    | Can manage cutscenes and waypoints  |
| Admin      | op   | Can manage stages, reload, and dump |

## cutscene

Manage and play cutscenes.

| Subcommand                                                     | Permission | Description                                 |
|----------------------------------------------------------------|------------|---------------------------------------------|
| `list`                                                         | Gamemaster | List all cutscenes in the current dimension |
| `add <name>`                                                   | Gamemaster | Create a new cutscene at your position      |
| `remove <name>`                                                | Gamemaster | Remove a cutscene                           |
| `linearize <name>`                                             | Gamemaster | Make a 2-node cutscene a straight line      |
| `play <player> <name> [length] [easing] [holdStart] [holdEnd]` | Gamemaster | Play a cutscene for a player                |
| `cancel <player>`                                              | Gamemaster | Cancel a player's active cutscene           |
| `resetAll confirm`                                             | Gamemaster | Remove all cutscenes (requires `confirm`)   |

### Play parameters

- `length` -- duration in ticks (overrides default)
- `easing` -- `LINEAR`, `SINE_IN/OUT/IN_OUT`, `CUBIC_IN/OUT/IN_OUT`, `QUINT_IN/OUT/IN_OUT`, `BOUNCE_IN/OUT/IN_OUT`
- `holdStart` / `holdEnd` -- ticks to hold at path start/end

## waypoint

Manage in-world markers.

| Subcommand                        | Permission | Description                 |
|-----------------------------------|------------|-----------------------------|
| `add <pos> <name> [icon] [color]` | Gamemaster | Add a waypoint              |
| `remove <pos>`                    | Gamemaster | Remove waypoint at position |
| `clear`                           | Gamemaster | Clear all waypoints         |
| `list`                            | Gamemaster | List all waypoints          |

## stage

Manage player game stages.

| Subcommand                 | Permission | Description                   |
|----------------------------|------------|-------------------------------|
| `<targets> add <stage>`    | Admin      | Add a stage to players        |
| `<targets> remove <stage>` | Admin      | Remove a stage from players   |
| `<targets> clear`          | Admin      | Clear all stages from players |
| `<targets> list`           | Any        | List a player's stages        |

## screeneffect

Apply post-processing effects to players.

```
/engine screeneffect <players> <effect> <intro> <hold> [outro] [easing] [command]
```

| Parameter | Description                                          |
|-----------|------------------------------------------------------|
| `players` | Target player(s)                                     |
| `effect`  | Effect name (`black`, `circle`, `star`, `cinematic`) |
| `intro`   | Fade-in duration in ticks                            |
| `hold`    | Hold duration in ticks                               |
| `outro`   | Fade-out duration in ticks (optional)                |
| `easing`  | Easing type (optional)                               |
| `command` | Command to run at effect start (optional)            |

## reload

```
/engine reload
```

**Permission**: Admin

Reloads all bundles. Calls `onUnload()` on all entrypoints, then re-discovers and reloads everything.

## hand

```
/engine hand
```

**Permission**: Any

Shows information about the currently held item, including its registry name, NBT data, components, and more.

## eval

```
/engine eval <code>
```

**Permission**: Configurable

Execute arbitrary Groovy code inline. Useful for testing:

```
/engine eval println "Hello from the engine!"
/engine eval 2 + 2
/engine eval Common.getGameStageHandler().addStage(player, "test")
```

## dump

Dump registry data to a markdown file.

| Subcommand        | Permission | Description              |
|-------------------|------------|--------------------------|
| `all`             | Admin      | Dump all registries      |
| `registry <name>` | Admin      | Dump a specific registry |

Output is written to a markdown file in the game directory.

## test

Development/testing commands.

| Subcommand                             | Permission | Description                                          |
|----------------------------------------|------------|------------------------------------------------------|
| `display block\|item\|text\|all\|kill` | Admin      | Spawn/manage test display entities                   |
| `world open <name> <true\|false>`      | Admin      | Create a test runtime dimension (`true` = temporary) |
| `world delete\|unload <name>`          | Admin      | Delete or unload a test world                        |
| `fake spawn`                           | Admin      | Spawn a test mannequin                               |

## See also

- [Cutscenes](cutscenes) -- Cutscene creation and playback
- [Waypoints](waypoints) -- In-world markers
- [Stages](stages) -- Game stages system
- [Instanced Worlds](instanced-worlds) -- Runtime dimensions
- [Post-Processing](post-processing) -- Screen effects
