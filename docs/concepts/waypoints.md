# Waypoints

Waypoints are persistent, colored in-world markers with a name, icon, and position. They're useful for navigation, marking points of interest, or building a fast-travel system.

## Creating a Waypoint

```groovy
import de.luckymcdev.foundryengine.common.waypoint.Waypoint
import de.luckymcdev.foundryengine.common.Common

def waypoint = new Waypoint(
    "Spawn Point",           // name
    "star",                  // icon
    100, 64, -200,           // x, y, z
    0xFF44FF44               // ARGB color (green)
)

Common.getWaypointManager().addWaypoint(level, waypoint)
```

## Managing Waypoints

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

## Waypoint Commands

| Command | Description |
|---------|-------------|
| `/engine waypoint add <pos> <name> [icon] [color]` | Add a waypoint |
| `/engine waypoint remove <pos>` | Remove waypoint at position |
| `/engine waypoint clear` | Clear all waypoints |
| `/engine waypoint list` | List all waypoints |

## Waypoint Properties

| Property | Type | Description |
|----------|------|-------------|
| `name` | `String` | Display name |
| `icon` | `String` | Icon identifier (e.g., `"star"`, `"house"`) |
| `x`, `y`, `z` | `int` | Block position |
| `color` | `int` | ARGB hex color |

## See Also

- [Areas](areas) — Spatial zones with enter/leave/tick events
- [Commands](commands) — Waypoint command reference
