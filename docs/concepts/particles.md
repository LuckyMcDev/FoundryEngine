# Custom Particles

FoundryEngine's particle system goes beyond simple particle types. It uses a keyframe-driven animation system that lets you define how a particle's color, scale, position, velocity, and rotation change over its lifetime.

## ParticleBuilder

The `ParticleBuilder` is the primary API. See [Builders](builders) for the full method reference, but here's a quick recap of the particle-specific methods:

```groovy
import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder
import de.luckymcdev.foundryengine.api.builder.particle.ParticleLayer
import de.luckymcdev.foundryengine.common.easing.Easing

ParticleBuilder.create(Common.id("sparkle"))
    .alwaysShow()                           // Ignore particle settings
    .lifetime(30)                           // 30 ticks
    .layer(ParticleLayer.TRANSLUCENT)       // Render layer
    .color(Color.WHITE, Color.RED, Easing.SINE_IN)
    .scale(0.5f, 1.5f, Easing.SINE_OUT)
    .velocity(new Vector3d(0.0, 0.1, 0.0))
```

## Keyframe Sequences

For full control, use the keyframe data objects directly:

```groovy
import de.luckymcdev.foundryengine.client.particle.data.*

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
    .velocityData(new ParticleVelocityData(new KeyframeSequence<Vector3d>()
        .add(new Vector3d(0, 0.2, 0), 0f, Easing.LINEAR)
        .add(new Vector3d(0, 0, 0), 1f, Easing.SINE_OUT)))
```

### KeyframeSequence

The `KeyframeSequence<T>` class stores animation keyframes:

| Method | Description |
|--------|-------------|
| `add(T value, float timepoint, Easing easing)` | Add a keyframe at normalized time (0–1) |
| `getInterpolation(float progress)` | Get interpolated value at progress point |

Each keyframe has:
- **Value** — the keyframe value (Color, Float, Vector3d, etc.)
- **Timepoint** — normalized position in the sequence (0.0 to 1.0)
- **Easing** — the easing function for interpolating to this keyframe

### KeyframedParticleData Types

| Class | Generic Type | What it controls |
|-------|-------------|------------------|
| `ParticleColorData` | `Color` | Color over lifetime |
| `ParticleScaleData` | `Float` | Scale over lifetime |
| `ParticleSpeedData` | `Float` | Speed over lifetime |
| `ParticleVelocityData` | `Vector3d` | Velocity vector over lifetime |
| `ParticlePositionData` | `Vector3d` | Position offset over lifetime |
| `ParticleRotationData` | `Float` | Rotation over lifetime |

## Spawning Particles

Particles created with `ParticleBuilder` are registered through `BundleEvents.registry`:

```groovy
BundleEvents.registry {
    it.particles(sparkleParticle, complexFxParticle)
}
```

## See Also

- [Builders](builders) — ParticleBuilder full method reference
- [Easing Functions](easing) — Complete easing function reference
- [Registries](registries) — Registering particle types
