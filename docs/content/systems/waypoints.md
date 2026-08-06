# Waypoints

Waypoints are colored markers that appear in the world. They help players navigate or mark points of interest.

## Creating a waypoint

```groovy
import de.luckymcdev.foundryengine.common.waypoint.Waypoint
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.util.ChatIcons
import de.luckymcdev.foundryengine.common.util.color.Color

def waypoint = new Waypoint(
    "Spawn Point",              // Name
    ChatIcons.PLUS.getString(), // Icon
    100, 64, -200,              // x, y, z
    new Color(0xFF44FF44)       // ARGB color (green)
)

Common.getWaypointManager().addWaypoint(level, waypoint)
```

## Colors

Colors use ARGB hex: `0xAARRGGBB`, wrapped in `new Color(int)`.

| Color                   | Code                    |
|-------------------------|-------------------------|
| Red                     | `new Color(0xFFFF0000)` |
| Green                   | `new Color(0xFF44FF44)` |
| Blue                    | `new Color(0xFF0000FF)` |
| Semi-transparent purple | `new Color(0x44FF44FF)` |

## Manager API

```groovy
def manager = Common.getWaypointManager()

// Get all waypoints in a dimension
def waypoints = manager.getWaypoints(level.dimension())

// Client-side only (no persistence)
manager.addLocal(level.dimension(), waypoint)

// Remove
manager.removeWaypoint(level, 100, 64, -200)

// Clear all
manager.clearWaypoints(level)
```

## Commands

| Command                                            | What it does       |
|----------------------------------------------------|--------------------|
| `/engine waypoint add <pos> <name> [icon] [color]` | Add a waypoint     |
| `/engine waypoint remove <pos>`                    | Remove at position |
| `/engine waypoint clear`                           | Clear all          |
| `/engine waypoint list`                            | List all waypoints |

## Next

- [Areas](areas.md) — spatial zones
- [Editor](editor.md) — waypoint panel
