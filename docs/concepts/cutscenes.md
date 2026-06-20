# Cutscene System

The cutscene system lets you create Bezier-curve camera animations with timeline-based screen effects and server commands. Cutscenes are defined per-dimension, edited in-world with visual handles, and played back with full player control locking.

You should make cutscenes with the in game menu.

## Architecture

```
CutsceneManager (server-side singleton)
  └─ stores Cutscene objects per dimension
      ├─ BezierPath (cubic Bezier spline segments)
      ├─ Anchor rotations (per-path-point pitch/yaw)
      └─ CutsceneAttachment list (timeline events)
          ├─ EffectAttachment (screen effects)
          └─ CommandAttachment (server commands)

ClientCutsceneManager (client-side singleton)
  ├─ Plays back cutscenes via PlayingCutscene
  ├─ Drives camera position/rotation each tick
  └─ Renders editor handles in-world
```

## Creating a Cutscene

Cutscenes are managed on the server through `CutsceneManager`:

```groovy
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene
import de.luckymcdev.foundryengine.common.cutscene.model.bezier.BezierPath
import de.luckymcdev.foundryengine.common.cutscene.model.bezier.BezierPoint
import org.joml.Vector3d
import org.joml.Vector2d

// Create path points
def points = [
    new BezierPoint(new Vector3d(0, 64, 0), new Vector3d(0, 64, 0)),
    new BezierPoint(new Vector3d(10, 70, 10), new Vector3d(10, 70, 10))
]
def path = new BezierPath(points)
def cutscene = new Cutscene("my_cutscene",
    new Vector2d(0, 0),   // initial rotation (pitch, yaw)
    new Vector2d(-10, 90), // final rotation
    path)
cutscene.setDefaultLength(100)     // ticks
cutscene.setDefaultHoldStart(20)   // hold at start
cutscene.setDefaultHoldEnd(20)     // hold at end

// Register with the manager
def manager = Common.getCutsceneManager()
manager.add(serverLevel, cutscene)
```

## Adding Timeline Attachments

### Screen Effects

```groovy
import de.luckymcdev.foundryengine.common.cutscene.model.EffectAttachment

// Fade to black at 30% through the cutscene
def effect = new EffectAttachment(0.3f, "black", 10, 20, 10, "SINE_IN_OUT")
cutscene.addAttachment(effect)
```

Available screen effects: `none`, `black`, `circle`, `star`, `cinematic`

Parameters: `at` (normalized time 0-1), `introDuration`, `holdDuration`, `outroDuration` (all in ticks), `lerpType`.

### Server Commands

```groovy
import de.luckymcdev.foundryengine.common.cutscene.model.CommandAttachment

// Run a command at 50% through the cutscene
def cmd = new CommandAttachment(0.5f, "say Hello from the cutscene!", 0)
cutscene.addAttachment(cmd)
```

## Playing a Cutscene

Use the `/engine cutscene play` command:

```
/engine cutscene play <player> <name> [length] [easing] [holdStart] [holdEnd]
```

Parameters:
- `length` — duration in ticks (overrides default)
- `easing` — `LINEAR`, `SINE_IN`, `SINE_OUT`, `SINE_IN_OUT`, `CUBIC_IN`, `CUBIC_OUT`, `CUBIC_IN_OUT`, `QUINT_IN`, `QUINT_OUT`, `QUINT_IN_OUT`, `BOUNCE_IN`, `BOUNCE_OUT`, `BOUNCE_IN_OUT`
- `holdStart` / `holdEnd` — ticks to hold at start/end

Cancel: `/engine cutscene cancel <player>`

## Editing Cutscenes In-World

The cutscene editor lets you place and adjust path nodes directly in the game world:

1. Open the **Cutscene Panel** from the editor menu
2. Select a cutscene from the list
3. Use the in-world handles (draggable) to adjust control points
4. Add/remove nodes from the panel or by holding the editor item

The in-world renderer draws:
- Path splines between anchor points
- Tangent handles on each control point
- Anchor rotation indicators

## Cutscene Commands

| Command                                                                         | Description                                 |
|---------------------------------------------------------------------------------|---------------------------------------------|
| `/engine cutscene list`                                                         | List all cutscenes in the current dimension |
| `/engine cutscene add <name>`                                                   | Create a new cutscene at your position      |
| `/engine cutscene remove <name>`                                                | Remove a cutscene                           |
| `/engine cutscene linearize <name>`                                             | Make a 2-node cutscene a straight line      |
| `/engine cutscene play <player> <name> [length] [easing] [holdStart] [holdEnd]` | Play a cutscene                             |
| `/engine cutscene cancel <player>`                                              | Cancel a player's active cutscene           |
| `/engine cutscene resetAll confirm`                                             | Remove all cutscenes                        |

## Using Cutscenes from Scripts

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule

// Play a cutscene when player enters an area via a module
Common.getAreaManager().registerModuleType(new AreaEnterModule() {
    @Override
    Identifier id() { return Common.id("play_intro") }

    @Override
    void onEnter(ServerPlayer player, Area area) {
        def manager = Common.getCutsceneManager()
        def cutscene = manager.find(player.serverLevel(), "intro_cutscene")
        if (cutscene != null) {
            manager.playCutscene(player, cutscene.getName(), 100, "SINE_IN_OUT", 10, 10)
        }
    }
})
```

## See Also

- [Commands](commands) — All `/engine` commands
- [Easing Functions](easing) — Available easing types
- [Areas](areas) — Trigger cutscenes with spatial zones
