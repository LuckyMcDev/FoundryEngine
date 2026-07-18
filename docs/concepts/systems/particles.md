# Custom particles

> **⚠️ Particle system will be reworked.** The current keyframe-driven system is planned to be replaced with support for **Bedrock particle definitions** (Snowstorm format). The current API will continue working but expect breaking changes in a future release.

FoundryEngine's particle system uses a keyframe-driven animation system. It lets you define how a particle's colour, scale, position, velocity, and rotation change over its lifetime.

## ParticleBuilder

The `ParticleBuilder` provides a fluent API for defining particle behaviour:

```groovy
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder
import de.luckymcdev.foundryengine.client.particle.ParticleLayer
import de.luckymcdev.foundryengine.common.easing.Easing

ParticleBuilder.create(Common.id("sparkle"))
    .alwaysShow()                           // Ignore particle settings
    .lifetime(30)                           // 30 ticks
    .layer(ParticleLayer.TRANSLUCENT)       // Render layer
    .color(Color.WHITE, Color.RED, Easing.SINE_IN)
    .scale(0.5f, 1.5f, Easing.SINE_OUT)
    .velocity(new Vector3d(0.0, 0.1, 0.0))
```

### ParticleBuilder Methods

| Method                                | Description                        |
|---------------------------------------|------------------------------------|
| `alwaysShow()`                        | Ignore client particle settings    |
| `lifetime(int)`                       | Duration in ticks                  |
| `layer(ParticleLayer)`                | Render layer (OPAQUE, TRANSLUCENT) |
| `color(Color, Color, Easing)`         | Start/end colour with easing       |
| `scale(float, float, Easing)`         | Start/end scale with easing        |
| `velocity(Vector3d)`                  | Constant velocity                  |
| `rotation(float, float, Easing)`      | Start/end rotation with easing     |
| `colorData(KeyframedParticleData)`    | Full keyframe colour control       |
| `scaleData(KeyframedParticleData)`    | Full keyframe scale control        |
| `velocityData(KeyframedParticleData)` | Full keyframe velocity control     |
| `positionData(KeyframedParticleData)` | Full keyframe position control     |
| `rotationData(KeyframedParticleData)` | Full keyframe rotation control     |
| `speedData(KeyframedParticleData)`    | Full keyframe speed control        |

## Keyframe sequences

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

| Method                                         | Description                              |
|------------------------------------------------|------------------------------------------|
| `add(T value, float timepoint, Easing easing)` | Add a keyframe at normalized time (0-1)  |
| `getInterpolation(float progress)`             | Get interpolated value at progress point |

Each keyframe has:

- **Value** — the keyframe value (Color, Float, Vector3d, etc.)
- **Timepoint** — normalized position in the sequence (0.0 to 1.0)
- **Easing** — the easing function for interpolating to this keyframe

### KeyframedParticleData Types

| Class                  | Generic Type | What it controls              |
|------------------------|--------------|-------------------------------|
| `ParticleColorData`    | `Color`      | Colour over lifetime          |
| `ParticleScaleData`    | `Float`      | Scale over lifetime           |
| `ParticleSpeedData`    | `Float`      | Speed over lifetime           |
| `ParticleVelocityData` | `Vector3d`   | Velocity vector over lifetime |
| `ParticlePositionData` | `Vector3d`   | Position offset over lifetime |
| `ParticleRotationData` | `Float`      | Rotation over lifetime        |

## Spawning particles

Particles created with `ParticleBuilder` are registered through `BundleEvents.registry`:

```groovy
BundleEvents.registry {
    it.particles(sparkleParticle, complexFxParticle)
}
```

## Examples

### Simple Sparkle

```groovy
ParticleBuilder.create(Common.id("sparkle"))
    .alwaysShow()
    .lifetime(20)
    .color(Color.WHITE, Color.YELLOW, Easing.SINE_IN)
    .scale(0.3f, 0.8f, Easing.SINE_OUT)
    .velocity(new Vector3d(0, 0.05, 0))
```

### Complex Keyframe Effect

```groovy
def seq = new KeyframeSequence<Color>()
    .add(Color.RED, 0f, Easing.LINEAR)
    .add(Color.YELLOW, 0.3f, Easing.SINE_IN_OUT)
    .add(Color.WHITE, 0.6f, Easing.SINE_IN_OUT)
    .add(new Color(0, 0, 0, 0), 1f, Easing.LINEAR) // fade out

ParticleBuilder.create(Common.id("explosion"))
    .lifetime(40)
    .layer(ParticleLayer.TRANSLUCENT)
    .colorData(new ParticleColorData(seq))
    .scaleData(new ParticleScaleData(new KeyframeSequence<Float>()
        .add(0.1f, 0f, Easing.LINEAR)
        .add(3.0f, 1f, Easing.QUAD_OUT)))
```

## See also

- [Builders](../core/builders) -- ParticleBuilder full method reference
- [Easing Functions](easing) -- Complete easing function reference
- [Registries](../core/registries) -- Registering particle types
