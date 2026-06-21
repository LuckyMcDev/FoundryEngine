# In-Game Editor

FoundryEngine includes a full Dear ImGui-based in-game editor with dockable panels, theming, and in-world editing tools. It's the primary interface for building content without leaving the game.

## Opening the Editor

The editor is opened by pressing the editor keybind (default: F7). If configured, using the editor item also opens it. Once open, a top menu bar with categories and a main workspace area appear.

## Menu Bar Categories

### Editor

| Panel | Description |
|-------|-------------|
| **Area Panel** | Manage spatial zones |
| **Main Editor** | Text/code editor for scripts |

### Cutscene

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
| **Console** | In-game log viewer with filtering |
| **Stopwatch** | Performance timing utilities |
| **Catalogue** | Browse items and blocks |
| **Dev Tools** | Developer utilities |
| **Effect** | Post-processing effect controls |
| **Minecraft Tools** | Game mode, time, weather, and other controls |
| **Waypoint** | Waypoint management |

### View

| Panel | Description |
|-------|-------------|
| **Info** | Information display |
| **Theme Selector** | UI theme selection |

> **⚠️ Blueprint Editor has been removed.** The node-based visual scripting system is being reworked for a better solution. This section will be updated when a new system is available.

## Cutscene Editor

The in-world cutscene editor lets you:

- Place camera path nodes at your current position
- Drag Bezier control handles in-world
- Adjust anchor rotations (pitch/yaw) per node
- Add timeline attachments: screen effects and server commands
- Preview playback at any speed

## In-World Editing

When holding the editor item, in-world editing tools activate:

- **Cutscene editing**: Add or remove path nodes at your position with a click
- **Area editing**: Resize and reposition area zone boundaries
- Visual handles appear at all editable points (draggable)

## Themes

The editor ships with seven built-in themes:

| Theme | Description |
|-------|-------------|
| **Dark** | Default dark theme |
| **ModernDark** | Modern dark variant |
| **Cherry** | Cherry blossom inspired |
| **CatppuccinMocha** | Popular warm theme |
| **Veil** | Subtle veil theme |
| **Vidlib** | Video library style |
| **BessDark** | Bess dark variant |

Switch themes from the **Theme Selector Panel** under the View menu.

## Extending the Editor

Java addon developers can register custom panels via `RegisterPanelEvent`:

```java
@SubscribeEvent
public void onRegisterPanel(RegisterPanelEvent event) {
    event.register(new MyCustomPanel());
}
```

Panels extend `de.luckymcdev.foundryengine.client.editor.panel.Panel` and override `content()` for ImGui drawing and `tick()` for per-frame logic.

## See Also

- [Blueprints](blueprints) — Visual scripting format
- [Cutscenes](cutscenes) — Cutscene creation and editing
- [Areas](areas) — Area zone management
- [Waypoints](waypoints) — Waypoint management
