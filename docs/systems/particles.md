# Custom Particles

> **Note:** The particle system is being reworked to support Bedrock particle definitions. The current API works but expect changes.

FoundryEngine's particle system lets you animate particles over their lifetime — changing color, size, speed, and rotation as they float.

## Simple particle

```groovy
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder
import de.luckymcdev.foundryengine.client.particle.ParticleLayer
import de.luckymcdev.foundryengine.common.easing.Easing

ParticleBuilder.create(Common.id("sparkle"))
    .alwaysShow()
    .lifetime(30)
    .layer(ParticleLayer.TRANSLUCENT)
    .color(Color.WHITE, Color.RED, Easing.SINE_IN)
    .scaleData(new ParticleScaleData(new KeyframeSequence<Float>()
        .add(0.5f, 0f, Easing.LINEAR)
        .add(1.5f, 1f, Easing.SINE_OUT)))
    .velocity(new Vector3d(0.0, 0.1, 0.0))
```

## Particle builder methods

| Method                         | What it does                           |
|--------------------------------|----------------------------------------|
| `alwaysShow()`                 | Show even if player disabled particles |
| `lifetime(int)`                | Duration in ticks                      |
| `layer(ParticleLayer)`         | `OPAQUE` or `TRANSLUCENT`              |
| `color(start, end, easing)`    | Color transition                       |
| `scale(start, end, easing)`    | Size transition                        |
| `velocity(Vector3d)`           | Movement speed                         |
| `rotation(start, end, easing)` | Rotation over lifetime                 |

## Full keyframe control

For advanced animations, use keyframe sequences:

```groovy
ParticleBuilder.create(Common.id("complex_fx"))
    .lifetime(100)
    .layer(ParticleLayer.TRANSLUCENT)
    .colorData(new ParticleColorData(new KeyframeSequence<Color>()
        .add(Color.RED, 0f, Easing.LINEAR)
        .add(Color.YELLOW, 0.5f, Easing.SINE_IN_OUT)
        .add(Color.BLUE, 1f, Easing.LINEAR)))
    .scaleData(new ParticleScaleData(new KeyframeSequence<Float>()
        .add(0.5f, 0f, Easing.LINEAR)
        .add(2.0f, 0.5f, Easing.CUBIC_OUT)
        .add(0.0f, 1f, Easing.LINEAR)))
```

Each keyframe has: a value, a timepoint (0.0 to 1.0), and an easing function.

## Registration

```groovy
BundleEvents.registry {
    it.particles(myParticle)
}
```

## Next

- [Easing Functions](easing.md) — easing types for animations
- [Post-Processing](post-processing.md) — screen shader effects
