# Mesh Rendering

FoundryEngine includes a custom 3D rendering engine alongside OBJ model support for GPU rendering outside Minecraft's standard pipeline.

**Note:** This API is Java-only.

## MeshRenderer

The `MeshRenderer` (access via `Client.getMeshRenderer()`) provides two rendering paths:

### Immediate draw

```java
Client.getMeshRenderer().draw(pipeline, modelView, buffer -> {
    buffer.addVertex(pose, x, y, z)
        .setColor(r, g, b, a)
        .setNormal(pose, nx, ny, nz);
});
```

### Begin/DrawSession

```java
try (var session = Client.getMeshRenderer().begin(pipeline, modelView)) {
    session.withTexture("Sampler0", textureView, sampler);
    var buffer = session.buffer();
    buffer.addVertex(...);
}
```

## Built-in pipelines

| Pipeline                | Vertex Format                          | Use Case                |
|-------------------------|----------------------------------------|-------------------------|
| `POSITION`              | Position only                          | Uncolored geometry      |
| `POSITION_COLOR`        | Position + Color                       | Colored geometry        |
| `POSITION_COLOR_NORMAL` | Position + Color + Normal              | Lit models              |
| `POSITION_TEX_COLOR`    | Position + UV + Color                  | Textured geometry       |
| `DEBUG_LINES`           | Position + Color + Normal + Line Width | Debug lines             |
| `FILLED_THROUGH_WALLS`  | Position + Color                       | Always-visible overlays |

## OBJ models

### Loading

```java
var model = new ObjModel(Common.id("models/suzanne.obj"));
Client.getObjModelManager().registerObjModel(model);
```

### Rendering

```java
model.renderModel(viewMatrix, EngineRenderPipelines.POSITION_COLOR_NORMAL);
model.renderModel(viewMatrix, pipeline, r, g, b, a);
model.renderObjects(viewMatrix, pipeline);
model.getObject("Cube").render(pipeline, viewMatrix);
```

### Transforms

Each `ObjObject` has independent position, rotation, and scale:

```java
var obj = model.getObject("Cube");
obj.setPosition(10, 0, 0);
obj.setRotation(0, (float)Math.PI / 2, 0);
obj.setScale(2.0f);
```

## Shaders

20+ shaders are bundled under `assets/foundryengine/shaders/`. Core shaders include:

| Shader Pair               | Purpose                  |
|---------------------------|--------------------------|
| `core/position`           | Uncolored solid geometry |
| `core/position_color`     | Vertex-colored           |
| `core/position_color_lit` | Lit with normals         |
| `core/position_tex_color` | Textured                 |
| `core/lines`              | Line rendering           |

## World gizmo utilities

```java
// Render a box handle
WorldGizmo.renderBox(position, 0.125, Color.RED);

// Render a line
WorldGizmo.renderLine(from, to, Color.BLUE, 3);

// Render an outline box
WorldGizmo.renderOutline(aabb, Color.GREEN);

// Hit test
boolean hovered = WorldGizmo.isHovered(position, eye, look);
```

## Next

- [Post-Processing](post-processing.md) — shader effects
- [Editor](editor.md) — in-world editing handles
