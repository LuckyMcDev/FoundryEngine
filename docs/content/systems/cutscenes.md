# Cutscene System

The cutscene system lets you create camera animations with Bezier curves, screen effects, and server commands — all editable in-world.

## How cutscenes work

A cutscene is:

- A **Bezier path** (a smooth curve through several points in the world)
- **Anchor rotations** (where the camera looks at start and end)
- **Attachments** (screen effects or commands triggered at specific times)

## Creating a cutscene from a script

```groovy
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene
import de.luckymcdev.foundryengine.common.easing.BezierPath
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.Vec2

def path = new BezierPath(new Vec3(0, 64, 0))
def cutscene = new Cutscene("my_cutscene",
        new Vec2(0, 0),     // Start rotation (pitch, yaw)
        new Vec2(-10, 90),  // End rotation
        path)
cutscene.setDefaultLength(100)     // Duration in ticks (5 seconds)
cutscene.setDefaultHoldStart(20)   // Hold at start
cutscene.setDefaultHoldEnd(20)     // Hold at end

Common.getCutsceneManager().add(serverLevel.dimension(), cutscene)
```

## Adding timeline effects

### Screen effects

Fade the screen to black, show a circle wipe, cinematic bars, etc.:

```groovy
import de.luckymcdev.foundryengine.common.cutscene.model.EffectAttachment

// Fade to black at 30% through the cutscene
def effect = new EffectAttachment(0.3f, "black", 10, 20, 10, "SINE_IN_OUT")
cutscene.addAttachment(effect)
```

Available effects: `none`, `black`, `circle`, `star`, `cinematic`

Parameters: `at` (0-1 normalized time), `intro`, `hold`, `outro` duration (ticks), `lerpType` (easing name)

### Server commands

Run commands at specific points in the cutscene:

```groovy
import de.luckymcdev.foundryengine.common.cutscene.model.CommandAttachment

// Say something at 50%
def cmd = new CommandAttachment(0.5f, "say Hello from the cutscene!", 0)
cutscene.addAttachment(cmd)
```

The third parameter is the command source (0 = server console).

## Playing a cutscene

Use the `/engine` command:

```
/engine cutscene play <player> <name> [length] [easing] [holdStart] [holdEnd]
```

Cancel: `/engine cutscene cancel <player>`

## In-world editing

The cutscene editor (open the Cutscene Panel from the editor menu) lets you:

1. Place path nodes at your current position
2. Drag Bezier handles in the world
3. Adjust pitch/yaw per node
4. Preview playback

## Commands

| Command                                     | What it does                       |
|---------------------------------------------|------------------------------------|
| `/engine cutscene list`                     | List cutscenes in this dimension   |
| `/engine cutscene add <name>`               | Create a cutscene at your position |
| `/engine cutscene remove <name>`            | Remove a cutscene                  |
| `/engine cutscene play <player> <name> ...` | Play a cutscene                    |
| `/engine cutscene cancel <player>`          | Cancel a cutscene                  |

## Next

- [Easing Functions](easing.md) — easing types for animations
- [Areas](areas.md) — trigger cutscenes when players enter zones
