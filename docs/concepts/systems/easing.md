# Easing functions

FoundryEngine includes Robert Penner's 31 easing functions for smooth animations, plus CSS-style cubic bezier easings. They are used throughout the engine in particle keyframes, cutscene playback, screen effects, and builders.

## Core Method

All easings extend from a common abstract method:

```groovy
float ease(float value, float min, float max, float time)
```

Where `time` is normalized (0.0 to 1.0) and the result is the eased value between `min` and `max`.

## Complete List

| Constant | Description |
|----------|-------------|
| `LINEAR` | No easing, constant speed |
| `QUAD_IN` | Quadratic acceleration |
| `QUAD_OUT` | Quadratic deceleration |
| `QUAD_IN_OUT` | Quadratic acceleration + deceleration |
| `CUBIC_IN` | Cubic acceleration |
| `CUBIC_OUT` | Cubic deceleration |
| `CUBIC_IN_OUT` | Cubic acceleration + deceleration |
| `QUARTIC_IN` | Quartic acceleration |
| `QUARTIC_OUT` | Quartic deceleration |
| `QUARTIC_IN_OUT` | Quartic acceleration + deceleration |
| `QUINTIC_IN` | Quintic acceleration |
| `QUINTIC_OUT` | Quintic deceleration |
| `QUINTIC_IN_OUT` | Quintic acceleration + deceleration |
| `SINE_IN` | Sinusoidal acceleration |
| `SINE_OUT` | Sinusoidal deceleration |
| `SINE_IN_OUT` | Sinusoidal acceleration + deceleration |
| `EXPO_IN` | Exponential acceleration |
| `EXPO_OUT` | Exponential deceleration |
| `EXPO_IN_OUT` | Exponential acceleration + deceleration |
| `CIRC_IN` | Circular acceleration |
| `CIRC_OUT` | Circular deceleration |
| `CIRC_IN_OUT` | Circular acceleration + deceleration |
| `ELASTIC_IN` | Elastic bounce (overshoots start) |
| `ELASTIC_OUT` | Elastic bounce (overshoots end) |
| `ELASTIC_IN_OUT` | Elastic bounce both ends |
| `BACK_IN` | Overshoots start then settles |
| `BACK_OUT` | Overshoots end then settles |
| `BACK_IN_OUT` | Overshoots both ends |
| `BOUNCE_IN` | Bounces at start |
| `BOUNCE_OUT` | Bounces at end |
| `BOUNCE_IN_OUT` | Bounces both ends |

## BezierEasing

CSS-style cubic bezier easings:

| Constant | Control Points |
|----------|----------------|
| `BezierEasing.EASE` | `(0.25, 0.1, 0.25, 1.0)` |
| `BezierEasing.EASE_IN` | `(0.42, 0.0, 1.0, 1.0)` |
| `BezierEasing.EASE_OUT` | `(0.0, 0.0, 0.58, 1.0)` |
| `BezierEasing.EASE_IN_OUT` | `(0.42, 0.0, 0.58, 1.0)` |

Custom: `new BezierEasing("custom", 0.1, 0.8, 0.2, 1.0)`

## Usage

```groovy
import de.luckymcdev.foundryengine.common.easing.Easing
import de.luckymcdev.foundryengine.common.easing.BezierEasing

// In builders
builder.color(Color.RED, Color.BLUE, Easing.CUBIC_IN_OUT)
builder.rotation(0f, (float)Math.PI * 2, Easing.ELASTIC_OUT)
builder.scale(0.5f, 1.5f, Easing.SINE_OUT)

// In keyframe sequences
new ParticleScaleData(new KeyframeSequence<Float>()
    .add(0.5f, 0f, Easing.LINEAR)
    .add(2.0f, 0.5f, Easing.BOUNCE_OUT)
    .add(0.0f, 1f, Easing.LINEAR))

// Bezier easings
builder.color(Color.RED, Color.BLUE, BezierEasing.EASE_IN_OUT)
```

## Used by

- **Particles** -- Keyframe-driven colour, scale, position, velocity, rotation over lifetime
- **Cutscenes** -- Camera path interpolation, hold start/end transitions
- **Post-Processing** -- Screen effect intro/hold/outro transitions
- **Builders** -- Item, block, and effect animations

## See also

- [Particles](particles) -- Keyframe-driven particle animations
- [Cutscenes](cutscenes) -- Cutscene playback easing
- [Post-Processing](post-processing) -- Screen effect transitions
