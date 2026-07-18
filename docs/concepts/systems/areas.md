# Areas

Areas are spatial zones that can be extended with **modules**. These are reusable stateless singletons that define behaviour per zone. They replace the old event-based API with a modular, composable system.

## Area Shapes

Areas come in two shapes:

- **`AABBArea`** — an axis-aligned bounding box (min/max corners), good for rooms, regions, forests
- **`BlockArea`** — a single block position, lighter for one-block triggers (e.g. a tree trunk or pressure plate)

Both share the `Area` base type, so all CRUD, module, and persistence APIs work the same way.

### Creating an AABBArea

```groovy
import de.luckymcdev.foundryengine.common.area.AABBArea
import de.luckymcdev.foundryengine.common.Common

def area = AABBArea.of(
        Common.id("my_zone"),
        new Vec3(10, 64, 10),   // min corner
        new Vec3(20, 80, 20),   // max corner
        level.dimension(),
        Color.RED
)
Common.getAreaManager().register(level, area)
```

### Creating a BlockArea

```groovy
import de.luckymcdev.foundryengine.common.area.BlockArea

def area = BlockArea.of(
        Common.id("tree_trunk"),
        new BlockPos(100, 64, 100),   // single block position
        level.dimension(),
        Color.GREEN
)
Common.getAreaManager().register(level, area)
```

## AreaEvents.register

Create areas automatically when a level loads:

```groovy
import de.luckymcdev.foundryengine.common.event.AreaEvents

AreaEvents.register { ServerLevel level ->
    def area = AABBArea.of(
            Common.id("my_zone"),
            new Vec3(0, 64, 0), new Vec3(10, 70, 10),
            level.dimension(), Color.RED)
    Common.getAreaManager().register(level, area)
}
```

This fires for every `ServerLevel` (overworld, nether, end, custom) as it loads.

## Module System

Modules are stateless singletons registered by type in `AreaManager`, then attached to areas by module ID. Per-area configuration is stored in `area.getModuleData(moduleId)` as a `CompoundTag`.

### Registering a Module Type

```groovy
import de.luckymcdev.foundryengine.common.area.module.AreaTickModule

def MID_HEAL = Common.id("heal")

Common.getAreaManager().registerModuleType(new AreaTickModule() {
    @Override
    Identifier id() { return MID_HEAL }

    @Override
    void tick(ServerLevel level, Area area) {
        float amount = area.getModuleData(MID_HEAL).getFloat("amount", 1.0f)
        level.players().each { entity ->
            if (entity instanceof LivingEntity) {
                entity.heal(amount)
            }
        }
    }
})
```

### Attaching a Module to an Area

```groovy
area.addModule(Common.id("heal"))
```

### Available Module Interfaces

| Interface          | Method                                                                | When it fires                        |
|--------------------|-----------------------------------------------------------------------|--------------------------------------|
| `AreaTickModule`   | `tick(ServerLevel, Area)`                                             | Every tick while entities are inside |
| `AreaEnterModule`  | `onEnter(ServerPlayer, Area)`                                         | A player enters the area             |
| `AreaLeaveModule`  | `onLeave(ServerPlayer, Area)`                                         | A player leaves the area             |
| `AreaBlockModule`  | `onBlockBreak(ServerLevel, Area, BlockPos, BlockState, ServerPlayer)` | Block break attempted                |
|                    | `onBlockPlace(ServerLevel, Area, BlockPos, BlockState, ServerPlayer)` | Block place attempted                |
| `AreaRenderModule` | `render(ClientLevel, Area, PoseStack, MultiBufferSource, float)`      | Debug/effect rendering (client)      |

Use `onAttach` / `onDetach` on `AreaModule` for setup and cleanup.

### Per-Area Module Data

```groovy
def data = area.getModuleData(Common.id("heal"))
data.putFloat("amount", 2.0f)
```

The tag persists with the area and is restored on reload.

## Presets

Presets bundle area creation with module assignment and configuration:

```groovy
import de.luckymcdev.foundryengine.common.area.preset.AreaPreset

def healingPreset = AreaPreset.builder("healing_zone")
        .module(Common.id("heal"))
        .moduleData(Common.id("heal"), { it.putFloat("amount", 2.0f) })
        .build()

Common.getAreaManager().registerPreset(healingPreset)
```

Create an area from the preset:

```groovy
def preset = Common.getAreaManager().getPreset("healing_zone")
def zone = preset.create(
        Common.id("my_zone"),
        min, max, level.dimension(), new Color(0x44FF8888))
Common.getAreaManager().register(level, zone)
```

## Linked Areas

Areas can reference each other by name:

```groovy
area.linkArea("canopy", Common.id("forest_roof"))
def linkedId = area.getLinkedArea("canopy")
def linked = Common.getAreaManager().getArea(linkedId)
```

## Manager Operations

```groovy
def manager = Common.getAreaManager()

// List all areas in a dimension
def areas = manager.getAreasForDimension(level.dimension())

// Update an existing area
manager.update(level, updatedArea)

// Remove an area
manager.remove(level, area)

// Look up by ID (global)
def area = manager.getArea(Common.id("my_zone"))
```

## Debug Visualization

Areas render with a coloured debug outline in-game when the editor is active. The colour is set at creation time via the `color` parameter (ARGB hex format).

## Complete Example

```groovy
package mybundle

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.area.Area
import de.luckymcdev.foundryengine.common.area.module.AreaTickModule
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule
import de.luckymcdev.foundryengine.common.area.preset.AreaPreset

class Entrypoint implements BundleEntrypoint {

    private static final Identifier MID_HEAL = Common.id("heal")
    private static final Identifier MID_WELCOME = Common.id("welcome")

    @Override
    void onLoad() {
        Common.getAreaManager().registerModuleType(new AreaTickModule() {
            @Override
            Identifier id() { return MID_HEAL }

            @Override
            void tick(ServerLevel level, Area area) {
                float amount = area.getModuleData(MID_HEAL).getFloat("amount", 1.0f)
                level.players().each { entity ->
                    if (entity instanceof LivingEntity) entity.heal(amount)
                }
            }
        })

        Common.getAreaManager().registerModuleType(new AreaEnterModule() {
            @Override
            Identifier id() { return MID_WELCOME }

            @Override
            void onEnter(ServerPlayer player, Area area) {
                String msg = area.getModuleData(MID_WELCOME).getString("message").orElse("")
                player.sendSystemMessage(Component.literal(msg))
            }
        })

        def preset = AreaPreset.builder("healing_spring")
                .module(MID_HEAL)
                .module(MID_WELCOME)
                .moduleData(MID_HEAL, { it.putFloat("amount", 1.0f) })
                .moduleData(MID_WELCOME, {
                    it.putString("message",
                            "You found the healing spring!")
                })
                .build()
        Common.getAreaManager().registerPreset(preset)

        BundleEvents.vanillaGame {
            def level = MinecraftServer.getServer().overworld()
            def preset = Common.getAreaManager().getPreset("healing_spring")
            def zone = preset.create(Common.id("healing_spring"),
                    new Vec3(0, 64, 0), new Vec3(10, 70, 10),
                    level.dimension(), new Color(0x4400FF88))
            Common.getAreaManager().register(level, zone)
        }
    }

    @Override
    void onUnload() {}
}
```

## See also

- [Cutscenes](cutscenes) -- Trigger cinematics with area events
- [Stages](stages) -- Gate content based on area entry
- [Editor](editor) -- Area editor panel
