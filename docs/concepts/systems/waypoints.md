# Waypoints

Waypoints are persistent, coloured in-world markers with a name, icon, and position. Useful for navigation, marking points of interest, or building a fast-travel system.

## Creating a waypoint

```groovy
import de.luckymcdev.foundryengine.common.waypoint.Waypoint
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.util.ChatIcons

def waypoint = new Waypoint(
    "Spawn Point",              // name
    ChatIcons.PLUS.getString(), // icon
    100, 64, -200,              // x, y, z
    0xFF44FF44                  // ARGB colour (green)
)

Common.getWaypointManager().addWaypoint(level, waypoint)
```

## Manager API

```groovy
def manager = Common.getWaypointManager()

// Get all waypoints in a dimension
def waypoints = manager.getWaypoints(level.dimension())

// Add a client-side only waypoint (no persistence)
manager.addLocal(level.dimension(), waypoint)

// Remove a waypoint at a position
manager.removeWaypoint(level, 100, 64, -200)

// Clear all waypoints in a dimension
manager.clearWaypoints(level)
```

## Waypoint properties

| Property | Type | Description |
|----------|------|-------------|
| `name` | `String` | Display name |
| `icon` | `String` | Icon identifier (e.g. `ChatIcons.PLUS`) |
| `x`, `y`, `z` | `int` | Block position |
| `color` | `int` | ARGB hex colour |

### Colour format

Colours use ARGB hex format: `0xAARRGGBB`. For example:
- `0xFFFF0000` — opaque red
- `0x44FF44FF` — semi-transparent purple
- `0xFF44FF44` — opaque green

### ChatIcons Utility

`de.luckymcdev.foundryengine.common.util.ChatIcons` provides a set of predefined icon strings (`PLUS`, `STAR`, `CROSS`, `ARROW`, etc.) suitable for waypoint markers.

## Waypoint Commands

| Command | Description |
|---------|-------------|
| `/engine waypoint add <pos> <name> [icon] [color]` | Add a waypoint |
| `/engine waypoint remove <pos>` | Remove waypoint at position |
| `/engine waypoint clear` | Clear all waypoints |
| `/engine waypoint list` | List all waypoints |

## See also

- [Editor](editor) -- Waypoint panel in the editor
- [Commands](commands) -- Full waypoint command reference
