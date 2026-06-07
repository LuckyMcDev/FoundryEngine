# In-Game Editor

FoundryEngine includes a full Dear ImGui-based in-game editor with dockable panels, theming, and in-world editing tools. It's the primary interface for building content without leaving the game.

## Opening the Editor

The editor is opened by using the editor item (if configured) or by pressing the editor keybind. Once open, you'll see a top menu bar with categories and a main workspace area.

## Panel Categories

### Editor

| Panel | Description |
|-------|-------------|
| **Blueprints Panel** | Create and edit node-based visual scripts |
| **Area Panel** | Manage spatial zones |
| **Main Editor** | Text/code editor for scripts |

### Cutscene Editor

| Panel | Description |
|-------|-------------|
| **Cutscene Panel** | List, create, delete, and configure cutscenes |
| **Cutscene Timeline Panel** | Timeline-based attachment editor for screen effects and commands |

### File Explorer

| Panel | Description |
|-------|-------------|
| **File Explorer** | Dual-pane file browser (local files + server resources) |
| **Resource Explorer** | Browse game resources (textures, models, sounds) |
| **Texture Viewer** | View image files |
| **Code Editor** | Syntax-highlighted code editing |

### Tools

| Panel | Description |
|-------|-------------|
| **Console Panel** | In-game log viewer with filtering |
| **Stopwatch Panel** | Performance timing utilities |
| **Catalogue Panel** | Browse items and blocks |
| **Dev Tools Panel** | Developer utilities |
| **Effect Panel** | Post-processing effect controls |
| **Minecraft Tools Panel** | Game mode, time, weather, and other controls |
| **Waypoint Panel** | Waypoint management |

### View

| Panel | Description |
|-------|-------------|
| **Info Panel** | Information display |
| **Theme Selector Panel** | UI theme selection |

## Blueprint Editor

The blueprint editor provides a node-based visual scripting environment:

- Drag-and-drop node placement
- Pin connections with type validation (Exec, Bool, Int, Float, String, Object, Any)
- Built-in event nodes for all API events
- Real-time graph execution
- Grid snapping and zoom

See [Blueprints](blueprints) for the full blueprint file format reference.

## Cutscene Editor

The in-world cutscene editor lets you:

- Place camera path nodes at your current position
- Drag Bezier control handles in-world
- Adjust anchor rotations per node
- Add timeline events (screen effects, commands)
- Preview playback at any speed

## In-World Editing

Hold the editor item to activate in-world editing tools:
- **Cutscene editing**: Add/remove path nodes at your position
- **Area editing**: Resize and reposition area zones
- Visual handles appear at editable points

## Theming

The editor comes with several built-in themes:

- **Dark** — Default dark theme
- **ModernDark** — Modern dark variant
- **Cherry** — Cherry blossom inspired
- **CatppuccinMocha** — Popular warm theme
- **Veil** — Subtle veil theme
- **Vidlib** — Video library style
- **BessDark** — Bess dark variant

Switch themes from the **Theme Selector Panel**.

## Extending the Editor

There will be a way for bundles to easily add panels soon

Java addon developers can register custom panels:

```java
@SubscribeEvent
public void onRegisterPanel(RegisterPanelEvent event) {
    event.register(new MyCustomPanel());
}
```

Panels extend `Panel` and override `content()` for ImGui drawing and `tick()` for per-frame logic.

## See Also

- [Blueprints](blueprints) — Visual scripting format
- [Cutscenes](cutscenes) — Cutscene creation and editing
- [Events](events) — `RegisterPanelEvent` reference
- [Areas](areas) — Area zone management
