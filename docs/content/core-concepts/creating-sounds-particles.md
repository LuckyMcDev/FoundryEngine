# Creating Sounds & Particles

## Sounds

Use `SoundBuilder` (in `de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder`) to create custom sound events:

```groovy
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder

SoundBuilder.create(id("my_music"))
    .subtitle("My Custom Music")
    .addSound(id("music.my_music"))
    .range(16.0f)

SoundBuilder.create(id("explosion"))
    .addSound(id("explosion_1"), 1.0f, 1.0f, 3, false, 16, false)
    .addSound(id("explosion_2"), 1.0f, 1.0f, 2, false, 16, false)
    .replace(true)
```

Place your `.ogg` sound files in `assets/my_namespace/sounds/`.

### SoundBuilder methods

| Method                                                                    | What it does                               |
|---------------------------------------------------------------------------|--------------------------------------------|
| `range(float)`                                                            | How far the sound travels                  |
| `subtitle(String)`                                                        | Text shown in sound settings               |
| `replace(boolean)`                                                        | Replace existing sounds with the same name |
| `addSound(location, volume, pitch, weight, stream, attenuation, preload)` | Add a sound file with full control         |

## Particles

Use `ParticleBuilder` (in `de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder`) to create custom particle effects.

> **Note:** The particle system is being reworked. It still works but expect changes in a future release.

### Simple particle

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

### ParticleBuilder methods

| Method                         | What it does                             |
|--------------------------------|------------------------------------------|
| `alwaysShow()`                 | Ignore client particle settings          |
| `lifetime(int)`                | Duration in ticks (default: 20)          |
| `layer(ParticleLayer)`         | Render layer (`OPAQUE` or `TRANSLUCENT`) |
| `color(start, end, easing)`    | Color transition over lifetime           |
| `scale(start, end, easing)`    | Size transition over lifetime            |
| `velocity(Vector3d)`           | Constant movement speed                  |
| `rotation(start, end, easing)` | Rotation over lifetime                   |

### Keyframe particles

For full control, use keyframe sequences:

```groovy
def seq = new KeyframeSequence<Color>()
    .add(Color.RED, 0f, Easing.LINEAR)
    .add(Color.YELLOW, 0.5f, Easing.SINE_IN_OUT)
    .add(Color.BLUE, 1f, Easing.LINEAR)

ParticleBuilder.create(Common.id("complex_fx"))
    .lifetime(100)
    .layer(ParticleLayer.TRANSLUCENT)
    .colorData(new ParticleColorData(seq))
```

Each keyframe has: a value, a timepoint (0.0 to 1.0), and an easing function.

## Registration

Register both sounds and particles in `BundleEvents.registry`:

```groovy
BundleEvents.registry {
    it.sounds(mySound, anotherSound)
    it.particles(myParticle, anotherParticle)
}
```

## Next

- [Events Guide](events-guide.md) — react to game events
- [Registration](registration.md) — how registration works
