# Skybox

The **Skybox** system lets you replace the default Minecraft sky with a large custom model rendered around the player at extreme scale. It is implemented as an `ItemDisplay` entity carrying a custom item model, scaled up so it always surrounds the camera.

## How it works

A single invisible `ItemDisplay` entity is spawned at the player's position and given a model via an item held under a custom `ITEM_MODEL` component. The model is scaled to `65000` so it behaves like an enormous skybox wrapper. The manager keeps the display parented to the player and re-moves it every tick.

Access is via `Client.getSkyboxManager()`.

```groovy
import de.luckymcdev.foundryengine.client.Client

Client.getSkyboxManager().setSkyboxItem(myItemStack)
```

Calling `setSkyboxItem(...)` with a stack that carries an `ITEM_MODEL` pointing at your skybox model replaces the current sky.

## Configuration

The feature is gated by the `CUSTOM_SKYBOX` client config option (default `false`):

- `client/src/main/resources/...` or in-game config → `CUSTOM_SKYBOX`

When disabled, the manager does nothing.

## Notes

- The item model id for the default fallback is `skybox`; provide a model with that id if you want the auto-created skybox.
- It is **client-side** only and requires a `.json` item model resource.

## Related

- [Mesh Rendering](mesh-rendering.md) — the custom 3D model pipeline used to render sky models
- [Editor](editor.md) — in-game tooling