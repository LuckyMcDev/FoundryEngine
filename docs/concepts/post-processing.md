# Post-Processing Effects

FoundryEngine includes a priority-based shader effect system that lets you apply post-processing effects to the game view.

## Built-in Effects

| Effect      | Priority | Description                |
|-------------|----------|----------------------------|
| `bloom`     | 10       | Bloom/glow effect          |
| `blur`      | 100      | Screen blur                |
| `grayscale` | 55       | Desaturate the screen      |
| `invert`    | 50       | Invert colors              |
| `creeper`   | 10       | Creeper-vision green tint  |
| `spider`    | 10       | Spider-vision tint         |
| `depth_vis` | 40       | Depth buffer visualization |

## Managing Effects

The effect manager is accessible from the client:

```groovy
import de.luckymcdev.foundryengine.client.Client

// Enable an effect
Client.getEffectManager().setEffectActive(
    Common.id("bloom"), 10, true)

// Disable an effect
Client.getEffectManager().disable(
    Common.id("grayscale"))

// Register a custom effect
Client.getEffectManager().register(
    new PrioritizedEffect(Common.id("my_effect"), 20))

// Clear all effects
Client.getEffectManager().clearAllEffects()
```

## Priority System

Effects are ordered by priority (lower = rendered first). When multiple effects are active, they composite in priority order. You can change an effect's priority when enabling it:

```groovy
// Enable bloom with custom priority
Client.getEffectManager().setEffectActive(
    Common.id("bloom"), 5, true)
```

## Screeneffect Command

Apply screen effects to players via command:

```
/engine screeneffect <players> <effect> <intro> <hold> [outro] [easing]
```

- `intro` — fade-in ticks
- `hold` — hold duration ticks
- `outro` — fade-out ticks (optional)
- `easing` — easing type (optional)

## See Also

- [Commands](commands) — Screeneffect command reference
- [Easing Functions](easing) — Easing types for effect transitions
