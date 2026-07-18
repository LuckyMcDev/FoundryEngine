# Game stages

Game Stages let you gate content behind named progression milestones. Add stages to players and use them to block access to items, blocks, mobs, dimensions, loot, recipes, and crafting.

## How stages work

Stages are namespaced identifiers (like `foundryengine:boss_defeated`). Players either have a stage or they don't. When a stage is added or removed, the engine fires cancellable events so other code can block the change.

Typical flow:

```
Player joins -> has no stages
    |
Player defeats boss -> addStage(player, foundryengine:dragon_slayer)
    |
Gated content is now accessible
```

## Stage registry

Stages can be registered with display names, descriptions, and parent stages. When a stage is granted, all its parents are granted too.

```groovy
def registry = Common.getGameStageHandler().getStageRegistry()

registry.register(
        Common.id("dragon_slayer"),
        Component.literal("Dragon Slayer"),
        Component.literal("Defeated the Ender Dragon"),
        Common.id("nether_access")  // parent stage, auto-granted
)
```

## Stage metadata

Each registered stage has a `StageMetadata` record with three fields:

- `displayName` -- a human-readable Component
- `description` -- a short Component
- `parents` -- list of parent stage IDs, auto-granted with the child

## API: GameStageHandler

Access via `Common.getGameStageHandler()`:

| Method                                           | Description                                                                  |
|--------------------------------------------------|------------------------------------------------------------------------------|
| `addStage(Player, Identifier)`                   | Add a stage. Fires cancellable event. Returns false if already present.      |
| `removeStage(Player, Identifier)`                | Remove a stage. Fires cancellable event. Returns false if missing.           |
| `addStages(Player, Collection<Identifier>)`      | Bulk add. Returns count actually added.                                      |
| `removeStages(Player, Collection<Identifier>)`   | Bulk remove. Returns count actually removed.                                 |
| `clearStages(Player)`                            | Remove all stages from a player.                                             |
| `hasStage(Player, Identifier)`                   | Check if a player has a stage.                                               |
| `getStages(Player)`                              | Returns all stages as an unmodifiable Set.                                   |
| `addStageIf(StageAdditionCondition, Identifier)` | Deferred addition. Condition checked once on next server tick, then removed. |

### Deferred stage addition

Queues a stage to be granted when a condition becomes true. The condition is checked once and discarded.

```groovy
def stages = Common.getGameStageHandler()
stages.addStageIf(
        { player -> player.getY() > 200 },
        Common.id("high_flier")
)
```

## Stage addons

Stage addons gate specific content. Each addon is a singleton on `GameStageHandler`:

```groovy
def stages = Common.getGameStageHandler()
```

### Item stages

Gates item possession, pickup, use, crafting, smelting, and attacks. Scans player inventory every 20 ticks and ejects gated items the player no longer qualifies for.

```groovy
stages.item().requireStages(Items.DIAMOND_SWORD, Common.id("weapons_tier2"))
stages.item().requireStages(Items.ELYTRA,
    Component.literal("Defeat the Ender Dragon first!"),
        Common.id("dragon_slayer"))
```

Gated events:

- Inventory scan (drops gated items)
- Item pickup (`ItemEntityPickupEvent.Pre`)
- Right-click with item, right-click on block, entity interact
- Attacking an entity while holding the item
- Item crafted or smelted (result zeroed)
- Tooltip (shows required stages)

### Block stages

Gates block interaction, breaking, drops, harvestability, and break speed.

```groovy
stages.blocks().requireStages(Blocks.ANCIENT_DEBRIS, Common.id("nether_access"))
```

Gated events:

- Right-click block (cancelled)
- Block break (cancelled)
- Block drops (cleared)
- Harvest check (set to false)
- Break speed (set to 0)

### Mob stages

Gates entity spawning, attacking, interaction, targeting, and despawn. Supports replacement spawning and configurable spawn ranges.

```groovy
stages.mobs().requireStages(EntityType.WITHER, Common.id("nether_complete"))

// Replace gated creepers with pigs instead of blocking spawn outright
stages.mobs().addReplacement(EntityType.CREEPER, EntityType.PIG)

// Let stage-locked mobs spawn from mob spawners
stages.mobs().setBypassSpawner(EntityType.BLAZE, true)

// Set a custom detection range for nearby players with the stage (default 64)
stages.mobs().setSpawnRange(EntityType.WITHER, 128)
```

Gated events:

- Mob spawn position check and placement check (FAIL if no nearby player has the stage)
- Entity join (cancelled, replacement spawned if configured)
- Attacking the entity (cancelled)
- Interacting with the entity (cancelled)
- Mob target change (cancelled if targeting an unqualified player)
- Mob despawn (forced if no qualified player is nearby)

### Dimension stages

Blocks dimension travel for players who lack the required stage. Creative mode bypasses this check by default.

```groovy
stages.dimensions().requireStages(
    ResourceKey.create(Registries.DIMENSION,
        ResourceLocation.parse("minecraft:the_end")),
        Common.id("end_open"))
```

Extra methods:

- `setBypassCreative(boolean)` -- turn creative bypass on or off
- `isBypassCreative()` -- read the current setting

### Loot stages

Gates loot tables. Clears entity drops when the killer lacks the stage, and blocks interaction with gated containers. This addon has known bugs and does not work reliably.

```groovy
stages.loot().requireStages(
    ResourceKey.create(Registries.LOOT_TABLE,
        ResourceLocation.parse("minecraft:chests/end_city")),
        Common.id("end_city_access"))
```

### Recipe stages

Gates crafting and smelting by result item ID. If the player lacks the stage for an item, the result is zeroed. This addon has known bugs and does not work reliably.

```groovy
stages.recipes().requireStages(
    ResourceLocation.parse("minecraft:netherite_ingot"),
        Common.id("nether_complete"))
```

## Stage tables

Stage tables award random stages from a weighted pool when players meet the entry conditions. Each entry has a stage, a weight, and optional prerequisite conditions.

```groovy
def tables = Common.getStageTableManager()
def lootTable = tables.createTable(Common.id("dungeon_loot"))

// 40% weight. Requires "entered_dungeon" stage.
lootTable.createEntry(Common.id("basic_weapon"), 40)
        .addStageCondition(Common.id("entered_dungeon"))

// 10% weight. Requires XP level 20.
lootTable.createEntry(Common.id("rare_artifact"), 10)
        .addCondition(player -> player.experienceLevel >= 20)

// Award via command:
// /engine stage @p table award foundryengine:dungeon_loot
```

Commands:

- `/engine stage <targets> table award <table>` -- award a random stage, inlines messages
- `/engine stage <targets> table silentaward <table>` -- same, no messages
- `/engine stage <targets> table list` -- list all registered tables

## Script example

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.event.EntityEvents

def stages = Common.getGameStageHandler()

// Grant stage on boss kill
EntityEvents.death {
    def mob = it.entity
    if (mob.getType() == EntityType.ENDER_DRAGON) {
        def player = it.source.getEntity()
        if (player instanceof ServerPlayer) {
            stages.addStage(player, Common.id("dragon_slayer"))
        }
    }
}

// Check stage on player tick
PlayerEvents.tick {
    if (stages.hasStage(it.player, Common.id("dragon_slayer"))) {
        // Grant special powers here
    }
}
```

## Stage events

Stage changes fire NeoForge events on `NeoForge.EVENT_BUS`:

| Event                    | Cancellable | When                      |
|--------------------------|-------------|---------------------------|
| `GameStageEvent.Add`     | Yes         | Before a stage is added   |
| `GameStageEvent.Remove`  | Yes         | Before a stage is removed |
| `GameStageEvent.Added`   | No          | After stage is added      |
| `GameStageEvent.Removed` | No          | After stage is removed    |

```groovy
import de.luckymcdev.foundryengine.common.game.stage.GameStageEvent

@SubscribeEvent
void onStageAdd(GameStageEvent.Add event) {
    if (event.getStage() == Common.id("too_early")) {
        event.setCanceled(true)
    }
}

@SubscribeEvent
void onStageAdded(GameStageEvent.Added event) {
    println "${event.getEntity().name} reached stage: ${event.getStage()}"
}
```

For integration mods, the `StageEvents` wrapper provides the same events with a simpler registration pattern:

```groovy
import de.luckymcdev.foundryengine.common.event.StageEvents

StageEvents.added { event ->
    println "${event.getEntity().name} reached stage: ${event.getStage()}"
}
```

## Commands

All stage commands are under `/engine stage`. Tab completion pulls from the stage registry and stage table manager.

```
/engine stage <targets> add <stage>        -- Add a stage (admin)
/engine stage <targets> remove <stage>     -- Remove a stage (admin)
/engine stage <targets> clear              -- Clear all stages (admin)
/engine stage <targets> list               -- List stages
/engine stage <targets> table award <table>  -- Award from table (admin)
/engine stage <targets> table silentaward <table>  -- Silent award (admin)
/engine stage <targets> table list           -- List tables
```

## Known issues

- Loot stages are experimental and buggy. They often fail with modded loot tables and container interactions.
- Recipe stages are experimental. They gate by result item ID, so items produced by multiple recipes are all locked together.

## See also

- [Areas](areas) -- Trigger stage changes when entering zones
- [Instanced Worlds](instanced-worlds) -- Gate dimension access
- [Commands](commands) -- Stage management commands
