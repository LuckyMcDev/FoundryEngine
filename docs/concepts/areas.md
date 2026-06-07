# Areas

Areas are axis-aligned bounding box (AABB) zones that track entity enter/leave/tick events. They're a simple but powerful tool for triggering game logic based on spatial position.

## Creating an Area

```groovy
import de.luckymcdev.foundryengine.common.area.Area
import de.luckymcdev.foundryengine.common.Common
import net.minecraft.world.phys.AABB
import org.joml.Vector3d

// Define the area bounds
def area = Area.of(
    "my_zone",                              // unique ID
    new Vec3(10, 64, 10),                   // min corner
    new Vec3(20, 80, 20),                   // max corner
    level.dimension(),                      // dimension
    0xFFFF4444                              // ARGB color (red)
)

// Register it (server-side)
Common.getAreaManager().register(level, area)
```

## Listening to Area Events

Areas fire events through the `AreaEvents` API:

```groovy
import de.luckymcdev.foundryengine.api.event.AreaEvents

AreaEvents.areaEnter {
    def area = it.area
    def entities = it.entities
    println "Entities entered ${area.id()}"
    entities.each { entity ->
        if (entity instanceof ServerPlayer) {
            entity.sendSystemMessage(
                Component.literal("Welcome to ${area.id()}!"))
        }
    }
}

AreaEvents.areaLeave {
    println "Entities left ${it.area.id()}"
}

AreaEvents.areaTick {
    // Runs every tick while entities are inside
    it.entities.each { entity ->
        if (entity instanceof ServerPlayer) {
            // Apply status effects, check conditions, etc.
        }
    }
}
```

## Event Context

All three events provide:

| Property | Type | Description |
|----------|------|-------------|
| `area` | `Area` | The area zone |
| `entities` | `List<Entity>` | Entities involved (enter/leave/tick) |

The `Area` record exposes:
- `id()` — string identifier
- `bounds()` — `AABB` bounding box
- `dimension()` — `ResourceKey<Level>`
- `color()` — debug outline color
- `contains(GlobalPos)` — position check

## Managing Areas

```groovy
def manager = Common.getAreaManager()

// List all areas in a dimension
def areas = manager.getAreasForDimension(level.dimension())

// Update an existing area
manager.update(level, updatedArea)

// Remove an area
manager.remove(level, area)
```

## Debug Visualization

Areas render with a colored debug outline in-game when the editor is active. The color is set at creation time via the `color` parameter (ARGB hex format).

## Use Cases

- **Trigger cutscenes** when player enters a region
- **Apply status effects** while inside a zone (poison, speed boost)
- **Protect areas** — cancel block breaking/placing
- **Create safe zones** — disable PvP or mob spawning
- **Build puzzle rooms** — track player presence per area
- **Spawn removal** — remove mobs that wander into restricted zones

## Complete Example

```groovy
package mybundle

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.area.Area
import de.luckymcdev.foundryengine.api.event.AreaEvents
import de.luckymcdev.foundryengine.api.event.BundleEvents

class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        // Create a healing zone
        BundleEvents.vanillaGame {
            def level = MinecraftServer.getServer().overworld()
            def healZone = Area.of("healing_spring",
                new Vec3(0, 64, 0), new Vec3(10, 70, 10),
                level.dimension(), 0x4400FF88)
            Common.getAreaManager().register(level, healZone)
        }

        // Heal players inside the zone
        AreaEvents.areaTick {
            if (it.area.id() == "healing_spring") {
                it.entities.each { entity ->
                    if (entity instanceof LivingEntity) {
                        entity.heal(1.0f)
                    }
                }
            }
        }
    }

    @Override
    void onUnload() {}
}
```

## See Also

- [Events](events) — Full event reference
- [Waypoints](waypoints) — Persistent in-world markers
- [Cutscenes](cutscenes) — Trigger cinematics with area events
