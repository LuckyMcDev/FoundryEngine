# Mesh Rendering & OBJ

FoundryEngine includes a custom mesh rendering engine alongside OBJ model loading support, providing direct GPU rendering outside Minecraft's standard pipeline.

## MeshRenderer

The `MeshRenderer` is a low-level rendering engine that uses a `MappableRingBuffer` for efficient vertex upload. Access via `Client.getMeshRenderer()`.

### Two Rendering Paths

#### Immediate draw()

Accumulates vertices, uploads, and submits in one call:

```java
Client.getMeshRenderer().draw(pipeline, modelView, buffer -> {
    buffer.addVertex(pose, x, y, z)
        .setColor(r, g, b, a)
        .setNormal(pose, nx, ny, nz);
});
```

#### Begin / DrawSession

For multi-step rendering with texture bindings:

```java
try (var session = Client.getMeshRenderer().begin(pipeline, modelView)) {
    session.withTexture("Sampler0", textureView, sampler);
    var buffer = session.buffer();
    buffer.addVertex(...);
    buffer.addVertex(...);
    // session.finish() called automatically by close()
}
```

### TextureBinding

```java
MeshRenderer.TextureBinding.of("Sampler0", textureView, sampler);
MeshRenderer.TextureBinding.of("Sampler0", textureView);
```

### RenderPipeline

The `MeshRenderer` works with any `RenderPipeline` — use the built-in pipelines or define your own.

## EngineRenderPipelines

Pre-built render pipelines for common use cases:

| Pipeline | Vertex Format | Use Case |
|----------|---------------|----------|
| `POSITION` | `POSITION`, QUADS | Uncoloured geometry |
| `POSITION_COLOR` | `POSITION_COLOR`, QUADS | Vertex-coloured geometry |
| `POSITION_COLOR_NORMAL` | `POSITION_COLOR_NORMAL`, QUADS | Lit models (Suzanne) |
| `POSITION_TEX_COLOR` | `POSITION_TEX_COLOR`, QUADS | Textured geometry |
| `DEBUG_LINES` | `POSITION_COLOR_NORMAL_LINE_WIDTH`, LINES | Debug line rendering |
| `FILLED_THROUGH_WALLS` | `POSITION_COLOR`, QUADS | Debug overlays (always visible) |

### RenderType Helpers

```java
// Standard translucent render type
MeshRenderer.renderType(vertexShader, fragmentShader, texture0, texture1);

// Cutout (no blending) render type
MeshRenderer.cutoutRenderType(vertexShader, fragmentShader, texture0, texture1);
```

These create cached `RenderType` instances with the correct pipeline setup and depth handling.

## OBJ Model System

The OBJ subsystem loads Wavefront `.obj` files and renders them through the `MeshRenderer`.

### Loading an OBJ Model

```java
import de.luckymcdev.foundryengine.client.render.obj.ObjModel;
import de.luckymcdev.foundryengine.client.Client;

var model = new ObjModel(Common.id("models/suzanne.obj"));
Client.getObjModelManager().registerObjModel(model);
// model.loadModel() called automatically at startup
```

Models are loaded from `assets/[namespace]/` paths. Suzanne (`suzanne.obj`) is loaded automatically at startup.

### Rendering

```java
// Via MeshRenderer with a pipeline
model.renderModel(viewMatrix, EngineRenderPipelines.POSITION_COLOR_NORMAL);
model.renderModel(viewMatrix, pipeline, r, g, b, a);

// Per-object rendering
model.renderObjects(viewMatrix, pipeline);
model.getObject("Cube").render(pipeline, viewMatrix);
```

### Key Types

| Class | Description |
|-------|-------------|
| `ObjModel` | Loaded model with faces and named objects |
| `ObjObject` | Named sub-object with position, rotation, scale |
| `ObjParser` | Parses `.obj` files (vertices, normals, UVs, faces) |
| `Face` | A polygon face (3+ vertices) with centroid calculation |
| `Vertex` | Position, normal, and UV for a single vertex |

### ObjObject Transform Properties

Each `ObjObject` has independent transform:

```java
var obj = model.getObject("Cube");
obj.setPosition(10, 0, 0);
obj.setRotation(0, (float)Math.PI / 2, 0);
obj.setScale(2.0f);
```

The transform is applied as a local model matrix when rendering.

## Shader System

The engine ships 20+ shaders under `assets/foundryengine/shaders/`:

### Core Shaders

| Shader Pair | Purpose |
|-------------|---------|
| `core/position` | Uncoloured solid geometry |
| `core/position_color` | Vertex-coloured geometry |
| `core/position_color_lit` | Lit geometry with normals |
| `core/position_tex_color` | Textured geometry |
| `core/lines` | Line rendering |
| `core/worldmesh` | World-aligned mesh |
| `core/worldmesh_color` | Coloured world-aligned mesh |

### Post-Processing Shaders

Located in `shaders/post/`: `black`, `cinematic`, `circle`, `depth_vis`, `grayscale`, `sepia`, `star`.

## EngineSceneDepth

Depth-aware rendering support via `EngineSceneDepth`:

```java
// Snapshot world depth for use in custom shaders
EngineSceneDepth.update();

// Get OpenGL depth texture ID
int glId = EngineSceneDepth.snapshotDepthGlId();
```

The depth texture is registered as `foundryengine:engine_scene_depth` and can be sampled in custom shaders.

## HandleRenderer

3D transform gizmo helpers for in-world editing:

```java
import de.luckymcdev.foundryengine.client.render.HandleRenderer;
import de.luckymcdev.foundryengine.common.util.color.Color;

// Render a draggable handle
HandleRenderer.renderHandle(position, Color.RED);
HandleRenderer.renderHandle(position, 0.25, Color.RED, Color.TRANSPARENT_RED);

// Render a line between points
HandleRenderer.renderLine(from, to, Color.BLUE, 3);

// Render an outline box
HandleRenderer.renderOutline(aabb, Color.GREEN);

// Hit test
boolean hovered = HandleRenderer.isHovered(position, eye, look, 0.5);
```

## Using Custom Pipelines

Create a custom pipeline JSON at `assets/foundryengine/pipeline/[name].json`:

```json
{
  "vertex_shader": "foundryengine:core/position_color",
  "fragment_shader": "foundryengine:core/position_color",
  "vertex_format": "POSITION_COLOR",
  "primitive": "QUADS"
}
```

Then reference it via `EngineRenderPipelines` or build programmatically with `RenderPipeline.builder()`.

## See Also

- [Post-Processing](post-processing) — Shader effects
- [Editor](editor) — In-world editing handles
- [Particles](particles) — Particle rendering
