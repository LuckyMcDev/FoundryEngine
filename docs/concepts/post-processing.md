# Post-Processing Effects

FoundryEngine includes a priority-based shader post-processing system that lets you apply custom effects to the game view. Effects are registered through `PostEffects` and rendered at configurable phases in the frame.

## Getting Started

### Registering an Effect

Effects are registered through the static `PostEffects` API. The `id` must match a pipeline JSON file under `assets/foundryengine/post_effect/[name].json`.

```java
import de.luckymcdev.foundryengine.client.post.PostEffects;
import de.luckymcdev.foundryengine.client.post.PostEffectHandle;
import de.luckymcdev.foundryengine.client.post.RenderPhase;
import de.luckymcdev.foundryengine.common.Common;

// Always active
PostEffectHandle handle = PostEffects.register(Common.id("my_effect"));

// Conditional
PostEffectHandle handle = PostEffects.register(
    Common.id("my_effect"),
    () -> SomeClientState.isActive()
);

// Full configuration
PostEffectHandle handle = PostEffects.register(
    Common.id("my_effect"),
    cfg -> cfg
        .when(() -> SomeClientState.isActive())
        .phase(RenderPhase.POST_WORLD)
        .fadeIn(20)
        .fadeOut(10)
        .uniform("MyConfig", 0.5f)
);
```

### Runtime Control

```java
handle.disable();
handle.enable();
handle.setCondition(() -> player.isUnderwater());
handle.setFade(20, 10);
handle.uniform("MyConfig", newValue);
handle.unregister();
```

### Built-in Effects

Effects are registered through `Client.getPostEffectManager()`:

| Effect | Handle |
|--------|--------|
| Grayscale | `Client.getPostEffectManager().getGrayscale()` |
| Sepia | `Client.getPostEffectManager().getSepia()` |
| Fade to Black | `Client.getPostEffectManager().getBlack()` |

---

## Pipeline JSON Format

Place pipeline descriptors at `assets/[namespace]/post_effect/[name].json`.

### One-Pass Effect

```json
{
  "targets": {},
  "passes": [
    {
      "vertex_shader": "minecraft:core/screenquad",
      "fragment_shader": "foundryengine:post/grayscale",
      "inputs": [
        { "sampler_name": "In", "target": "minecraft:main" }
      ],
      "output": "minecraft:main"
    }
  ]
}
```

### Two-Pass Effect (most common)

When you need an intermediate framebuffer, use the swap target pattern:

```json
{
  "targets": {
    "swap": {}
  },
  "passes": [
    {
      "vertex_shader": "minecraft:core/screenquad",
      "fragment_shader": "foundryengine:post/my_effect",
      "inputs": [
        { "sampler_name": "In", "target": "minecraft:main" }
      ],
      "output": "swap",
      "uniforms": {}
    },
    {
      "vertex_shader": "minecraft:core/screenquad",
      "fragment_shader": "minecraft:post/blit",
      "inputs": [
        { "sampler_name": "In", "target": "swap" }
      ],
      "output": "minecraft:main",
      "uniforms": {
        "BlitConfig": [
          { "name": "ColorModulate", "type": "vec4", "value": [1.0, 1.0, 1.0, 1.0] }
        ]
      }
    }
  ]
}
```

### Fields

| Field | Description |
|---|---|
| `targets` | Local intermediate framebuffers for multi-pass effects |
| `passes` | Ordered list of render passes, each a fullscreen quad |
| `vertex_shader` | Use `"minecraft:core/screenquad"` for all effects |
| `fragment_shader` | Namespaced path to your `.fsh` shader (without extension) |
| `inputs` | Samplers bound for this pass: `sampler_name` + `target` |
| `output` | Target framebuffer: local name or `"minecraft:main"` |
| `uniforms` | Map of block name to member descriptors |

### Uniform Member Descriptor

| Field | Description |
|---|---|
| `name` | GLSL member variable name inside the block |
| `type` | `"float"`, `"int"`, `"vec2"`, `"vec3"`, `"vec4"`, `"mat4"` |
| `value` | Default value (number for scalars, array for vectors/matrices) |

---

## GLSL Shader Conventions

### File Location

```
assets/[namespace]/shaders/post/[name].fsh
```

### Version

Always use `#version 330` (or `#version 150` for simpler effects).

### Vertex Input

```glsl
in vec2 texCoord;
```

Provided by `minecraft:core/screenquad`. No custom vertex shader needed.

### Sampler Naming

GLSL sampler name = JSON `sampler_name` + `"Sampler"` suffix:

| JSON `sampler_name` | GLSL uniform |
|---|---|
| `"In"` | `InSampler` |
| `"Depth"` | `DepthSampler` |

```glsl
uniform sampler2D InSampler;
```

### SamplerInfo Block

Always declare this in every pass:

```glsl
layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};
```

Automatically populated by the engine.

### Output

```glsl
out vec4 fragColor;
```

### Complete Example (Sepia)

```glsl
#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    float r = dot(color.rgb, vec3(0.393, 0.769, 0.189));
    float g = dot(color.rgb, vec3(0.349, 0.686, 0.168));
    float b = dot(color.rgb, vec3(0.272, 0.534, 0.131));
    fragColor = vec4(r, g, b, color.a);
}
```

---

## API Reference

### PostEffects (entry point)

All methods are static:

```java
PostEffectHandle register(Identifier id)
PostEffectHandle register(Identifier id, BooleanSupplier condition)
PostEffectHandle register(Identifier id, Consumer<PostEffectConfig> configurator)
```

Convenience methods:

```java
PostEffectHandle blur(Identifier id, float radius)
PostEffectHandle blur(Identifier id, DoubleSupplier radius)
PostEffectHandle vignette(Identifier id, float intensity)
PostEffectHandle vignette(Identifier id, DoubleSupplier intensity)
PostEffectHandle conditionalWithFade(Identifier id, BooleanSupplier condition, int fadeInTicks, int fadeOutTicks)
```

### PostEffectConfig (builder)

Methods return `this` for chaining:

```java
cfg.when(BooleanSupplier condition)
cfg.phase(RenderPhase phase)
cfg.priority(int priority)
cfg.externalTargets(Set<Identifier> targets)
cfg.externalTarget(Identifier id, Supplier<RenderTarget> supplier)
cfg.fadeIn(int ticks)
cfg.fadeOut(int ticks)
cfg.fade(int fadeInTicks, int fadeOutTicks)
cfg.onBeforeApply(Consumer<PostEffectContext> callback)
cfg.onAfterApply(Consumer<PostEffectContext> callback)

// Uniforms (blockName = GLSL uniform block name, not member name)
cfg.uniform(String blockName, float value)
cfg.uniform(String blockName, float x, float y)
cfg.uniform(String blockName, float x, float y, float z)
cfg.uniform(String blockName, float x, float y, float z, float w)
cfg.uniform(String blockName, int value)
cfg.uniform(String blockName, DoubleSupplier supplier)
cfg.uniform(String blockName, IntSupplier supplier)
cfg.uniformVec2(String blockName, Supplier<Vector2fc> supplier)
cfg.uniformVec3(String blockName, Supplier<Vector3fc> supplier)
cfg.uniformVec4(String blockName, Supplier<Vector4fc> supplier)
cfg.uniformMat4(String blockName, Supplier<Matrix4fc> supplier)
cfg.uniformRaw(String blockName, Supplier<List<UniformValue>> supplier)

// Texture overrides
cfg.texture(String samplerName, Identifier textureId)
```

### PostEffectHandle

```java
handle.enable()
handle.disable()
handle.setEnabled(boolean enabled)
handle.isEnabled()
handle.setCondition(BooleanSupplier condition)
handle.setPhase(RenderPhase phase)
handle.setPriority(int priority)
handle.setExternalTargets(Set<Identifier> targets)
handle.setFadeIn(int ticks)
handle.setFadeOut(int ticks)
handle.setFade(int inTicks, int outTicks)
handle.onBeforeApply(Consumer<PostEffectContext> callback)
handle.onAfterApply(Consumer<PostEffectContext> callback)
handle.uniform(String blockName, ...)       // same overloads as config
handle.uniformVec2(String blockName, ...)
handle.uniformVec3(String blockName, ...)
handle.uniformVec4(String blockName, ...)
handle.uniformMat4(String blockName, ...)
handle.uniformRaw(String blockName, ...)
handle.texture(String samplerName, Identifier textureId)
handle.isActive()
handle.getId()
handle.unregister()
```

### PostEffectContext

Passed to `onBeforeApply` / `onAfterApply` callbacks each frame:

```java
ctx.getClient()
ctx.getDeltaTick()
ctx.getScreenWidth()
ctx.getScreenHeight()
ctx.getProcessor()
```

### PostEffectManager

The `PostEffectManager` is a singleton owned by `Client`:

```java
// Access
var mgr = Client.getPostEffectManager();

// Registered effect handles
mgr.getGrayscale()
mgr.getSepia()
mgr.getBlack()
```

---

## RenderPhase

```java
RenderPhase.POST_WORLD   // After world render, before hand/overlay
RenderPhase.PRE_GUI      // After world + hand, before GUI
RenderPhase.POST_RENDER  // After everything (entire frame)
```

| Phase | Covers | Use Case |
|---|---|---|
| `POST_WORLD` | World only (depth available) | World-space effects, water distortion |
| `PRE_GUI` | World + items/hands | Full game-view effects before UI |
| `POST_RENDER` | Entire frame including GUI | Screen-wide overlays, color grading |

### Depth Snapshot Target

When using `POST_RENDER`, world-only depth is preserved and exposed as:

```
foundryengine:world_depth_snapshot
```

In a pipeline JSON:

```json
{
  "sampler_name": "WorldDepth",
  "target": "foundryengine:world_depth_snapshot",
  "use_depth_buffer": true
}
```

This is the most reliable way to read world-only depth in `POST_RENDER`, since vanilla clears the main depth buffer earlier in the frame.

---

## Priority Ordering

Multiple effects in the same phase are applied in **descending priority order**:

- Higher priority = applied first (bottom of the stack)
- Lower priority = applied last (closest to display)
- Default priority is `0`. Use negatives to run after defaults.

---

## Uniform Convention

All `.uniform()` calls take the **GLSL uniform block name** as the first argument, not an individual member name.

For a JSON block:

```json
"MyConfig": [
    { "name": "Strength", "type": "float", "value": 0.0 },
    { "name": "Color", "type": "vec4", "value": [1.0, 0.0, 0.0, 1.0] }
]
```

Override via:

```java
cfg.uniformRaw("MyConfig", () -> List.of(
    new UniformValue.FloatUniform(myStrength),
    new UniformValue.Vec4Uniform(new Vector4f(r, g, b, a))
));
```

Single-member blocks have typed shortcuts:

```java
cfg.uniform("Intensity", 0.8f);
cfg.uniformVec4("TintBlock", () -> new Vector4f(r, g, b, a));
```

---

## Fade System

When `fadeIn` / `fadeOut` is configured, the engine tracks a per-effect `intensity` (0.0 to 1.0):

- Condition `false → true`: ramps to 1.0 over `fadeIn` ticks
- Condition `true → false`: ramps to 0.0 over `fadeOut` ticks
- If ticks = 0, the transition is instant

The current intensity is injected each frame as the `Intensity` uniform block:

```glsl
layout(std140) uniform Intensity {
    float Value;
};
```

Declare this block and multiply your effect strength by `Intensity.Value` to honor the fade.

---

## UniformSuppliers Reference

`UniformSuppliers` provides factory methods for common uniform value suppliers:

### Constant
```java
UniformSuppliers.constant(float value)
UniformSuppliers.constant(float x, float y)
```

### Time
```java
UniformSuppliers.gameTime()
UniformSuppliers.partialTick()
```

### Screen
```java
UniformSuppliers.screenWidth()
UniformSuppliers.screenHeight()
UniformSuppliers.screenSize()
```

### Player State
```java
UniformSuppliers.playerHealth()
UniformSuppliers.playerHealthNorm()
UniformSuppliers.playerAir()
UniformSuppliers.playerAirNorm()
```

### Animation
```java
UniformSuppliers.sinTime(float speed)
UniformSuppliers.cosTime(float speed)
UniformSuppliers.pingPong(float min, float max, float speed)
```

### Wrappers
```java
UniformSuppliers.ofFloat(DoubleSupplier)
UniformSuppliers.ofInt(IntSupplier)
UniformSuppliers.ofVec2(Supplier<Vector2fc>)
UniformSuppliers.ofVec3(Supplier<Vector3fc>)
UniformSuppliers.ofVec4(Supplier<Vector4fc>)
UniformSuppliers.ofMat4(Supplier<Matrix4fc>)
```

---

## See Also

- [Editor](editor) — Effects panel for toggling effects at runtime
- [Commands](commands) — Command reference
- [Easing Functions](easing) — Easing types for effect transitions
