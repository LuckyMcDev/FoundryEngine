# Game Stages

Game Stages let you gate content behind named progression milestones. Add stages to players, and use them to control access to items, mobs, dimensions, loot, and recipes.

## How Stages Work

A stage is simply a string name (like `"boss_defeated"` or `"tutorial_complete"`). Players either have a stage or don't. When adding a stage, the engine fires cancellable events so other code can veto the addition.

```
Player joined -> has no stages
    |
Player defeats boss -> addStage(player, "boss_defeated")
    |
Gated content is now accessible
```

## API Reference

### GameStageHandler

Access via `Common.getGameStageHandler()`:

| Method | Description |
|--------|-------------|
| `addStage(Player, String)` | Add a stage. Returns false if already has it. |
| `removeStage(Player, String)` | Remove a stage. Returns false if missing. |
| `clearStages(Player)` | Remove all stages from a player. |
| `hasStage(Player, String)` | Check if player has a stage. |
| `getStages(Player)` | Get all stages as a Set. |
| `addStageIf(StageAdditionCondition, String)` | Deferred addition — checked on player tick. |

### Stage Addons

Stage addons let you gate specific content types:

```groovy
def stages = Common.getGameStageHandler()

// Gate items — blocks right-click, crafting, smelting
stages.item().requireStages(Items.DIAMOND_SWORD, "weapons_tier2")
stages.item().requireStages(Items.ELYTRA,
    Component.literal("Defeat the Ender Dragon first!"),
    "ender_dragon_defeated")

// Gate mobs — blocks spawn, attack, interaction
stages.mobs().requireStages(EntityType.WITHER, "nether_complete")

// Gate dimensions — blocks teleportation
stages.dimensions().requireStages(
    ResourceKey.create(Registries.DIMENSION,
        ResourceLocation.parse("minecraft:the_end")),
    "end_open")

// Gate loot tables — clears drops from gated tables (experimental)
stages.loot().requireStages(
    ResourceKey.create(Registries.LOOT_TABLE,
        ResourceLocation.parse("minecraft:chests/end_city")),
    "end_city_access")

// Gate recipes by ID (experimental)
stages.recipes().requireStages(
    ResourceLocation.parse("minecraft:netherite_ingot"),
    "nether_complete")
```

### Using Stages in Scripts

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule
import de.luckymcdev.foundryengine.api.event.EntityEvents
import de.luckymcdev.foundryengine.api.event.PlayerEvents

def stages = Common.getGameStageHandler()

// Grant stage on boss kill
EntityEvents.death {
    def mob = it.entity
    if (mob.getType() == EntityType.ENDER_DRAGON) {
        def player = it.source.getEntity()
        if (player instanceof ServerPlayer) {
            stages.addStage(player, "dragon_slayer")
        }
    }
}

// Check stage
PlayerEvents.tick {
    if (stages.hasStage(it.player, "dragon_slayer")) {
        // Grant special powers
    }
}
```

### Stage Events

Stage changes fire NeoForge events:

| Event | Cancellable | When |
|-------|-------------|------|
| `GameStageEvent.Add` | Yes | Before a stage is added |
| `GameStageEvent.Remove` | Yes | Before a stage is removed |
| `GameStageEvent.Added` | No | After stage is added |
| `GameStageEvent.Removed` | No | After stage is removed |

```groovy
import de.luckymcdev.foundryengine.common.game.stage.GameStageEvent

@SubscribeEvent
void onStageAdd(GameStageEvent.Add event) {
    if (event.getStageName() == "too_early") {
        event.setCanceled(true)
    }
}

@SubscribeEvent
void onStageAdded(GameStageEvent.Added event) {
    println "${event.getEntity().name} reached stage: ${event.getStageName()}"
}
```

### Deferred Stage Addition

Add a condition that's checked automatically on player tick:

```groovy
stages.addStageIf(
    { player -> player.getY() > 200 },
    "high_flier"
)
```

## Groovy Examples

```groovy
// Grant stage on entering a specific area
Common.getAreaManager().registerModuleType(new AreaEnterModule() {
    @Override Identifier id() { return Common.id("enter_stage") }
    @Override void onEnter(ServerPlayer player, Area area) {
        def stages = Common.getGameStageHandler()
        if (area.id().path == "tutorial_zone") {
            stages.addStage(player, "tutorial_complete")
        }
    }
})
```

## See Also

- [Areas](areas) — Trigger stage changes when entering zones
- [Instanced Worlds](instanced-worlds) — Gate dimension access
- [Commands](commands) — Stage management commands
