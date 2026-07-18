# Areas

Areas are spatial zones that detect when players enter, leave, or stand inside them. You can attach **modules** to add behavior like healing, damage, or welcome messages.

## Area shapes

| Shape         | What it is              | Good for                            |
|---------------|-------------------------|-------------------------------------|
| **AABBArea**  | A box (min/max corners) | Rooms, regions, forests             |
| **BlockArea** | A single block position | One-block triggers, pressure plates |

## Creating an area

```groovy
import de.luckymcdev.foundryengine.common.area.AABBArea
import de.luckymcdev.foundryengine.common.Common

def area = AABBArea.of(
    Common.id("my_zone"),
    new Vec3(10, 64, 10),   // min corner
    new Vec3(20, 80, 20),   // max corner
    level.dimension(),
    Color.RED               // Outline color in editor
)
Common.getAreaManager().register(level, area)
```

## Adding behavior with modules

Modules are reusable behaviors you attach to areas. FoundryEngine provides these module types:

| Module             | What it does                              |
|--------------------|-------------------------------------------|
| `AreaTickModule`   | Runs every tick while entities are inside |
| `AreaEnterModule`  | Runs when a player enters                 |
| `AreaLeaveModule`  | Runs when a player leaves                 |
| `AreaBlockModule`  | Runs on block break/place attempts        |
| `AreaRenderModule` | Runs for debug rendering (client)         |

### Example: healing zone

```groovy
def MID_HEAL = Common.id("heal")

// Register the module type
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

// Attach it to an area
area.addModule(Common.id("heal"))
```

### Example: welcome message

```groovy
def MID_WELCOME = Common.id("welcome")

Common.getAreaManager().registerModuleType(new AreaEnterModule() {
    @Override Identifier id() { return MID_WELCOME }

    @Override void onEnter(ServerPlayer player, Area area) {
        player.sendSystemMessage(
            Component.literal("Welcome to ${area.id()}!"))
    }
})
```

## Presets

Presets bundle area creation with modules and configuration:

```groovy
def healingPreset = AreaPreset.builder("healing_zone")
    .module(Common.id("heal"))
    .moduleData(Common.id("heal"), { it.putFloat("amount", 2.0f) })
    .build()

Common.getAreaManager().registerPreset(healingPreset)

// Use the preset
def zone = healingPreset.create(
    Common.id("my_zone"), min, max, level.dimension(), Color.GREEN)
Common.getAreaManager().register(level, zone)
```

## Manager operations

```groovy
def manager = Common.getAreaManager()

// List areas in a dimension
def areas = manager.getAreasForDimension(level.dimension())

// Look up by ID
def area = manager.getArea(Common.id("my_zone"))

// Remove
manager.remove(level, area)
```

## Creating areas when a level loads

Use `LevelEvents.load` to create areas when a dimension loads:

```groovy
import de.luckymcdev.foundryengine.common.event.LevelEvents

LevelEvents.load { event ->
    def level = event.level
    if (level instanceof ServerLevel) {
        def area = AABBArea.of(
            Common.id("spawn_zone"),
            new Vec3(-10, 64, -10), new Vec3(10, 70, 10),
            level.dimension(), Color.RED)
        Common.getAreaManager().register(level, area)
    }
}
```

## Visuals

Areas show colored outlines in-game when the editor is active. The color is set when you create the area.

## Next

- [Waypoints](waypoints.md) — add markers to the world
- [Cutscenes](cutscenes.md) — trigger cutscenes from area entry
- [Editor](editor.md) — area editor panel
