# Game Stages

Game Stages let you gate content behind progression. Players earn stages by completing objectives, and stages control what items, mobs, dimensions, and recipes they can access.

## How stages work

A stage is a named identifier (like `dragon_slayer`). A player either has a stage or does not.

```
Player joins → no stages
    ↓
Player defeats boss → addStage(player, "dragon_slayer")
    ↓
Items gated behind "dragon_slayer" are now usable
```

## Adding stages to a player

```groovy
def stages = Common.getGameStageHandler()

// Add a stage
stages.addStage(player, Common.id("dragon_slayer"))

// Check if a player has a stage
if (stages.hasStage(player, Common.id("dragon_slayer"))) {
    // Grant special powers
}

// Remove a stage
stages.removeStage(player, Common.id("dragon_slayer"))
```

## Gating content

### Items

Block item possession, pickup, crafting, and use:

```groovy
stages.item().requireStages(Items.ELYTRA,
    Component.literal("Defeat the Ender Dragon first!"),
    Common.id("dragon_slayer"))
```

### Blocks

Block interaction, breaking, and drops:

```groovy
stages.blocks().requireStages(Blocks.ANCIENT_DEBRIS, Common.id("nether_access"))
```

### Mobs

Block spawning and interaction with certain mobs:

```groovy
stages.mobs().requireStages(EntityType.WITHER, Common.id("nether_complete"))

// Replace gated creepers with pigs instead
stages.mobs().addReplacement(EntityType.CREEPER, EntityType.PIG)
```

### Dimensions

Block dimension travel:

```groovy
stages.dimensions().requireStages(
    ResourceKey.create(Registries.DIMENSION,
        ResourceLocation.parse("minecraft:the_end")),
    Common.id("end_open"))
```

### Recipes

Gate crafting by result item:

```groovy
stages.recipes().requireStages(
    ResourceLocation.parse("minecraft:netherite_ingot"),
    Common.id("nether_complete"))
```

## Stage events

Stage changes fire events you can listen to:

```groovy
StageEvents.adding {
    println "${it.player} is gaining stage ${it.stage}"
}

StageEvents.added {
    println "${it.player} gained stage ${it.stage}"
}
```

## Commands

```
/engine stage <targets> add <stage>      -- Add a stage
/engine stage <targets> remove <stage>   -- Remove a stage
/engine stage <targets> clear            -- Clear all stages
/engine stage <targets> list             -- List stages
```

## Known issues

- Loot stages are experimental and buggy
- Recipe stages have known bugs

## Next

- [Areas](areas.md) — trigger stage changes when entering zones
- [Custom Worlds](instanced-worlds.md) — gate dimension access
