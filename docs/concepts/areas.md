# Areas

Areas are spatial zones that can be extended with **modules** — reusable stateless singletons that define behavior per zone. They replace the old event-based API with a modular, composable system.

## Area Shapes

Areas come in two shapes:

- **`AABBArea`** — an axis-aligned bounding box (min/max corners), good for rooms, regions, forests
- **`BlockArea`** — a single block position, lighter for one‑block triggers (e.g. a tree trunk)

Both share the `Area` base type, so all CRUD, module, and persistence APIs work the same way.

### Creating an AABBArea

```groovy
import de.luckymcdev.foundryengine.common.area.AABBArea
import de.luckymcdev.foundryengine.common.Common

def area = AABBArea.of(
    Identifier.fromNamespaceAndPath("mymod", "my_zone"),
    new Vec3(10, 64, 10),     // min corner
    new Vec3(20, 80, 20),     // max corner
    level.dimension(),
    Color.RED
)
Common.getAreaManager().register(level, area)
```

### Creating a BlockArea

```groovy
import de.luckymcdev.foundryengine.common.area.BlockArea

def area = BlockArea.of(
    Identifier.fromNamespaceAndPath("mymod", "tree_trunk"),
    new BlockPos(100, 64, 100),               // single block position
    level.dimension(),
    Color.GREEN
)
Common.getAreaManager().register(level, area)
```

## Adding Behavior with Modules

Instead of subscribing to the old `AreaEvents.areaEnter`/`areaLeave` etc., you register **module types** in `AreaManager` and attach them to areas by ID. Modules are stateless singletons — per‑area configuration is stored in `getModuleData(id)`.

If you need to create areas when a level loads, use `AreaEvents.register`:

```groovy
import de.luckymcdev.foundryengine.common.event.AreaEvents

AreaEvents.register { ServerLevel level ->
    def area = AABBArea.of(Common.id("my_zone"), new Vec3(0, 64, 0), new Vec3(10, 70, 10), level.dimension(), Color.RED)
    Common.getAreaManager().register(level, area)
}
```

This fires for every `ServerLevel` (overworld, nether, end, custom) as it loads — cleaner than wiring `LevelEvent.Load` yourself.

```groovy
import de.luckymcdev.foundryengine.common.area.module.AreaTickModule
import net.minecraft.resources.Identifier

def MID_HEAL = Identifier.fromNamespaceAndPath("mymod", "heal")

// Register a module type
Common.getAreaManager().registerModuleType(new AreaTickModule() {
    @Override
    Identifier id() { return MID_HEAL }

    @Override
    void tick(ServerLevel level, Area area) {
        // Runs every tick while entities are inside
        level.players().each { entity ->
            if (entity instanceof LivingEntity) {
                entity.heal(1.0f)
            }
        }
    }
})
```

Then create an area and assign it:

```groovy
import net.minecraft.resources.Identifier

def area = AABBArea.of(Identifier.fromNamespaceAndPath("mymod", "healing_zone"), ...)
area.addModule(Identifier.fromNamespaceAndPath("mymod", "heal"))
```

### Available Module Interfaces

| Interface | Method | When it fires |
|-----------|--------|---------------|
| `AreaTickModule` | `tick(ServerLevel, Area)` | Every tick while entities are inside |
| `AreaEnterModule` | `onEnter(ServerPlayer, Area)` | A player enters the area |
| `AreaLeaveModule` | `onLeave(ServerPlayer, Area)` | A player leaves the area |
| `AreaBlockModule` | `onBlockBreak(ServerLevel, Area, BlockPos, BlockState, ServerPlayer)` | Block break attempted |
| | `onBlockPlace(ServerLevel, Area, BlockPos, BlockState, ServerPlayer)` | Block place attempted |
| `AreaRenderModule` | `render(ClientLevel, Area, PoseStack, MultiBufferSource, float)` | Debug/effect rendering (client) |

All module methods return `void`. Use `onAttach`/`onDetach` on `AreaModule` for setup/cleanup.

### Per‑Area Module Data

Use `area.getModuleData(moduleId)` to get a `CompoundTag` for per‑area config:

```groovy
import net.minecraft.resources.Identifier

def mid = Identifier.fromNamespaceAndPath("mymod", "heal")
def data = area.getModuleData(mid)
data.putFloat("heal_amount", 2.0f)
```

The tag persists with the area and is restored on reload.

## Presets

Presets bundle area creation with module assignment and configuration:

```groovy
import de.luckymcdev.foundryengine.common.area.preset.AreaPreset
import net.minecraft.resources.Identifier

def MID_HEAL = Identifier.fromNamespaceAndPath("mymod", "heal")

def healingPreset = AreaPreset.builder("healing_zone")
    .module(MID_HEAL)
    .moduleData(MID_HEAL, { it.putFloat("amount", 2.0f) })
    .build()

Common.getAreaManager().registerPreset(healingPreset)
```

Then create an area from the preset and register it:

```groovy
import net.minecraft.resources.Identifier

def preset = Common.getAreaManager().getPreset("healing_zone")
def zone = preset.create(
    Identifier.fromNamespaceAndPath("mymod", "my_zone"),
    min, max, level.dimension(), new Color(0x44FF8888))
Common.getAreaManager().register(level, zone)
```

## Linked Areas

Areas can reference each other by name via `linkedAreas` (`Map<String, Identifier>`):

```groovy
import net.minecraft.resources.Identifier

area.linkArea("canopy", Identifier.fromNamespaceAndPath("mymod", "forest_roof"))
def linkedId = area.getLinkedArea("canopy")
def linked = Common.getAreaManager().getArea(linkedId)
```

## Managing Areas

```groovy
def manager = Common.getAreaManager()

// List all areas in a dimension
def areas = manager.getAreasForDimension(level.dimension())

// Update an existing area
manager.update(level, updatedArea)

// Remove an area
manager.remove(level, area)

// Look up by ID (global)
def area = manager.getArea(Identifier.fromNamespaceAndPath("mymod", "my_zone"))
```

## Debug Visualization

Areas render with a colored debug outline in‑game when the editor is active. The color is set at creation time via the `color` parameter (ARGB hex format).

## Use Cases

- **Trigger cutscenes** when player enters a region
- **Apply status effects** while inside a zone (poison, speed boost)
- **Protect areas** — cancel block breaking/placing via `AreaBlockModule`
- **Create safe zones** — disable PvP or mob spawning
- **Build puzzle rooms** — track player presence per area
- **Spawn removal** — remove mobs that wander into restricted zones

## Complete Example

```groovy
package mybundle

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.area.Area
import de.luckymcdev.foundryengine.common.area.module.AreaTickModule
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule
import de.luckymcdev.foundryengine.common.area.preset.AreaPreset
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer

class Entrypoint implements BundleEntrypoint {

    private static final Identifier MID_HEAL = Identifier.fromNamespaceAndPath("mymod", "heal")
    private static final Identifier MID_WELCOME = Identifier.fromNamespaceAndPath("mymod", "welcome")

    @Override
    void onLoad() {
        Common.getAreaManager().registerModuleType(new AreaTickModule() {
            @Override
            Identifier id() { return MID_HEAL }

            @Override
            void tick(ServerLevel level, Area area) {
                float amount = area.getModuleData(MID_HEAL)
                    .getFloat("amount", 1.0f)
                level.players().each { entity ->
                    if (entity instanceof LivingEntity) {
                        entity.heal(amount)
                    }
                }
            }
        })

        Common.getAreaManager().registerModuleType(new AreaEnterModule() {
            @Override
            Identifier id() { return MID_WELCOME }

            @Override
            void onEnter(ServerPlayer player, Area area) {
                String msg = area.getModuleData(MID_WELCOME)
                    .getString("message")
                    .orElse("")
                player.sendSystemMessage(
                    Component.literal(msg))
            }
        })

        def preset = AreaPreset.builder("healing_spring")
            .module(MID_HEAL)
            .module(MID_WELCOME)
            .moduleData(MID_HEAL, { it.putFloat("amount", 1.0f) })
            .moduleData(MID_WELCOME, { it.putString("message", "You found the healing spring!") })
            .build()

        Common.getAreaManager().registerPreset(preset)

        BundleEvents.vanillaGame {
            def level = MinecraftServer.getServer().overworld()
            def preset = Common.getAreaManager().getPreset("healing_spring")
            def zone = preset.create(
                Identifier.fromNamespaceAndPath("mymod", "healing_spring"),
                new Vec3(0, 64, 0), new Vec3(10, 70, 10),
                level.dimension(), new Color(0x4400FF88))
            Common.getAreaManager().register(level, zone)
        }
    }

    @Override
    void onUnload() {}
}
```

## See Also

- [Events](events) — Full event reference
- [Cutscenes](cutscenes) — Trigger cinematics with area events
- [Blueprints](blueprints) — Structural building patterns
