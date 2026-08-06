# In-Game Editor

FoundryEngine includes a full in-game editor built on [Dear ImGui](https://github.com/ocornut/imgui). It is the primary tool for creating content without leaving the game.

## Opening the editor

Press **F7** (default keybind). The editor opens as a dockable window with a top menu bar and workspace area.

## Menu categories

### Editor

| Panel           | What it does                    |
|-----------------|---------------------------------|
| **Area Panel**  | Create and manage spatial zones |
| **Main Editor** | Text/code editor for scripts    |

### Cutscene

| Panel                 | What it does                                    |
|-----------------------|-------------------------------------------------|
| **Cutscene Panel**    | List, create, delete, and configure cutscenes   |
| **Cutscene Timeline** | Timeline editor for screen effects and commands |

### File Explorer

| Panel                 | What it does                         |
|-----------------------|--------------------------------------|
| **File Explorer**     | Browse local and server files        |
| **Resource Explorer** | Browse game textures, models, sounds |
| **Texture Viewer**    | View image files                     |
| **Code Editor**       | Syntax-highlighted code editing      |

### Tools

| Panel               | What it does                      |
|---------------------|-----------------------------------|
| **Console**         | In-game log viewer with filtering |
| **Stopwatch**       | Performance timing                |
| **Catalogue**       | Browse items and blocks           |
| **Dev Tools**       | Developer utilities               |
| **Effect**          | Post-processing effect controls   |
| **Minecraft Tools** | Game mode, time, weather controls |
| **Waypoint**        | Manage waypoints                  |

### View

| Panel              | What it does        |
|--------------------|---------------------|
| **Info**           | Information display |
| **Theme Selector** | Choose editor theme |

## In-world editing

When using the editor item, you can:

- Place and drag cutscene path nodes
- Resize area zone boundaries
- Visual handles at all editable points

## Themes

Seven built-in themes: Dark, ModernDark, Cherry, CatppuccinMocha, Veil, Vidlib, BessDark

Switch themes from the **Theme Selector Panel** under View.

## Next

- [Cutscenes](cutscenes.md) — create and edit cutscenes
- [Areas](areas.md) — manage spatial zones
- [Waypoints](waypoints.md) — manage waypoints
