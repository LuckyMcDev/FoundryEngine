# Post-Processing Effects

Post-processing applies shader effects to the game view — things like grayscale, sepia, bloom, blur, or custom GLSL shaders.

**Note:** This API is Java-only. Bundle scripts cannot register post-processing effects directly.

## Getting started

```java
var mgr = Client.getPostEffectManager();

// Register an effect that is always active
PostEffectHandle handle = mgr.register(Common.id("my_effect"));

// Register with a condition
PostEffectHandle handle = mgr.register(
    Common.id("my_effect"),
    () -> player.isUnderwater()
);

// Full configuration
PostEffectHandle handle = mgr.register(
    Common.id("my_effect"),
    cfg -> cfg
        .when(() -> SomeCondition.isActive())
        .phase(RenderPhase.POST_WORLD)
        .fadeIn(20)
        .fadeOut(10)
        .uniform("MyConfig", 0.5f)
);
```

## Runtime control

```java
handle.disable();
handle.enable();
handle.setCondition(() -> player.isUnderwater());
handle.setFade(20, 10);
handle.unregister();
```

## Built-in effects

```java
mgr.getGrayscale();  // Monochrome
mgr.getSepia();      // Sepia tone
mgr.getBlack();      // Fade to black
mgr.getStar();       // Star wipe
mgr.getCircle();     // Circle wipe
mgr.getCinematic();  // Cinematic bars
```

## Render phases

| Phase         | When it runs                   | Use for                |
|---------------|--------------------------------|------------------------|
| `POST_WORLD`  | After world, before hand/GUI   | World-space effects    |
| `PRE_GUI`     | After world + hand, before GUI | Full game-view effects |
| `POST_RENDER` | After everything               | Screen-wide overlays   |

## Fade system

When you configure `fadeIn` / `fadeOut`, the engine smoothly transitions:

- Condition becomes true → intensity ramps to 1.0 over `fadeIn` ticks
- Condition becomes false → intensity fades to 0.0 over `fadeOut` ticks

The intensity is available in your GLSL shader as:

```glsl
layout(std140) uniform Intensity {
    float Value;
};
```

## Custom shaders

Place GLSL shaders in `assets/[namespace]/shaders/post/[name].fsh` and pipeline JSONs in `assets/[namespace]/post_effect/[name].json`.

## Next

- [Easing Functions](easing.md) — easing for effect transitions
- [Editor](editor.md) — effects panel for runtime toggling
