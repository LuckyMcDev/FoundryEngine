# Easing Functions

Easing functions make animations look smooth. FoundryEngine includes 31 Penner easing functions plus CSS-style cubic bezier easings.

## How easing works

All easings use the same formula:

```groovy
float result = Easing.SINE_IN_OUT.ease(currentTime, minValue, maxValue, totalDuration)
```

Where `currentTime` goes from 0 to 1 (normalized), and the result is the eased value between `min` and `max`.

## Quick list

| Constant                                  | Effect                           |
|-------------------------------------------|----------------------------------|
| `LINEAR`                                  | Constant speed, no easing        |
| `QUAD_IN`                                 | Slow start, fast end             |
| `QUAD_OUT`                                | Fast start, slow end             |
| `QUAD_IN_OUT`                             | Slow start and end               |
| `CUBIC_IN` / `CUBIC_OUT` / `CUBIC_IN_OUT` | Stronger versions of quad        |
| `SINE_IN` / `SINE_OUT` / `SINE_IN_OUT`    | Smooth sinusoidal                |
| `EXPO_IN` / `EXPO_OUT`                    | Very dramatic                    |
| `ELASTIC_IN` / `ELASTIC_OUT`              | Bouncy, overshoots               |
| `BOUNCE_IN` / `BOUNCE_OUT`                | Like a bouncing ball             |
| `BACK_IN` / `BACK_OUT`                    | Overshoots slightly then settles |

## Bezier easing

CSS-style cubic bezier:

```groovy
BezierEasing.EASE        // (0.25, 0.1, 0.25, 1.0)
BezierEasing.EASE_IN     // (0.42, 0.0, 1.0, 1.0)
BezierEasing.EASE_OUT    // (0.0, 0.0, 0.58, 1.0)
BezierEasing.EASE_IN_OUT // (0.42, 0.0, 0.58, 1.0)

// Custom
new BezierEasing("custom", 0.1, 0.8, 0.2, 1.0)
```

## Usage in builders

```groovy
builder.color(Color.RED, Color.BLUE, Easing.CUBIC_IN_OUT)
builder.rotation(0f, (float)Math.PI * 2, Easing.ELASTIC_OUT)
// Note: for scale transitions, use scaleData with a KeyframeSequence
```

## Usage in keyframes

```groovy
new ParticleScaleData(new KeyframeSequence<Float>()
    .add(0.5f, 0f, Easing.LINEAR)
    .add(2.0f, 0.5f, Easing.BOUNCE_OUT)
    .add(0.0f, 1f, Easing.LINEAR))
```

## Where easing is used

- **Particles** — color, scale, position over lifetime
- **Cutscenes** — camera path interpolation
- **Post-Processing** — screen effect transitions

## Next

- [Particles](particles.md) — particle animations
- [Cutscenes](cutscenes.md) — cutscene playback
