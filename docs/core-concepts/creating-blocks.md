# Creating Blocks

Blocks are the foundation of Minecraft builds. FoundryEngine provides `BlockBuilder` (in `de.luckymcdev.foundryengine.common.builder.block.BlockBuilder`) for creating custom blocks.

## Basic block

```groovy
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder

def myBlock = BlockBuilder.create(id("my_block"))
    .properties { it.strength(2.0f, 3.0f) }  // Hardness, resistance
```

## Block properties

```groovy
BlockBuilder.create(id("magic_block"))
    .properties { it
        .strength(2.0f, 3.0f)       // Hardness, explosion resistance
        .lightLevel { 15 }          // Emit light (0-15)
        .noCollision()              // Walk through it
        .noOcclusion()              // See through gaps
    }
```

## Block types

```groovy
// Block with no item (decoration only)
BlockBuilder.create(id("invisible_wall"))
    .noItem()
    .properties { it.noCollision().strength(-1.0f, 3600000.0f) }

// Block with a custom class (e.g. SlabBlock)
BlockBuilder.create(id("my_slab"))
    .factory { new SlabBlock(it) }
    .properties { it.noOcclusion().lightLevel { 15 } }
```

## Block variants

Shorthand methods for common block shapes. These set the underlying block class — callbacks and ghost mode use `EngineBlock` and can be combined with variant methods:

| Method                 | Parameters            | Block class             |
|------------------------|-----------------------|-------------------------|
| `.stairs(baseState)`   | `BlockState`          | `StairBlock`            |
| `.slab()`              | —                     | `SlabBlock`             |
| `.wall()`              | —                     | `WallBlock`             |
| `.fence()`             | —                     | `FenceBlock`            |
| `.fenceGate(type)`     | `WoodType`            | `FenceGateBlock`        |
| `.door(type)`          | `BlockSetType`        | `DoorBlock`             |
| `.trapdoor(type)`      | `BlockSetType`        | `TrapDoorBlock`         |
| `.pressurePlate(type)` | `BlockSetType`        | `PressurePlateBlock`    |
| `.button(type, ticks)` | `BlockSetType`, `int` | `ButtonBlock`           |
| `.pillar()`            | —                     | `RotatedPillarBlock`    |
| `.glass()`             | —                     | `TransparentBlock`      |
| `.bars()`              | —                     | `IronBarsBlock`         |
| `.carpet()`            | —                     | `CarpetBlock`           |
| `.chain()`             | —                     | `ChainBlock`            |
| `.lantern()`           | —                     | `LanternBlock`          |
| `.ladder()`            | —                     | `LadderBlock`           |
| `.endRod()`            | —                     | `EndRodBlock`           |
| `.lever()`             | —                     | `LeverBlock`            |
| `.observer()`          | —                     | `ObserverBlock`         |
| `.dispenser()`         | —                     | `DispenserBlock`        |
| `.dropper()`           | —                     | `DropperBlock`          |
| `.hopper()`            | —                     | `HopperBlock`           |
| `.anvil()`             | —                     | `AnvilBlock`            |
| `.grindstone()`        | —                     | `GrindstoneBlock`       |
| `.composter()`         | —                     | `ComposterBlock`        |
| `.redstoneLamp()`      | —                     | `RedstoneLampBlock`     |
| `.daylightDetector()`  | —                     | `DaylightDetectorBlock` |
| `.beacon()`            | —                     | `BeaconBlock`           |
| `.lightningRod()`      | —                     | `LightningRodBlock`     |

```groovy
BlockBuilder.create(id("my_stairs"))
        .stairs(baseBlockState)       // Pass the base block's default state
        .properties { it.strength(2.0f) }

BlockBuilder.create(id("my_door"))
        .door(BlockSetType.IRON)     // Or your own BlockSetType
        .properties { it.noOcclusion() }

BlockBuilder.create(id("my_glass"))
        .glass()
        .properties { it.noOcclusion().strength(0.3f) }
```

Variant methods set `blockFactory` internally — they're mutually exclusive with `.factory()`. Callbacks and ghost mode work with all variants.

## Block drops

Control what a block drops when mined:

```groovy
BlockBuilder.create(id("my_block"))
    .dropsSelf()                              // Drop itself (default)

BlockBuilder.create(id("my_ore"))
    .drops(Items.DIAMOND)                     // Drop a specific item

BlockBuilder.create(id("unbreakable"))
    .dropsNothing()                           // Drop nothing

BlockBuilder.create(id("custom_drop"))
    .dropsCustom { block ->                   // Custom loot table
        LootTable.lootTable().withPool(
            LootPool.lootPool().add(
                ItemLootEntry.lootTableItem(Items.DIAMOND)))
    }
```

## Block entities

Attach a block entity (tile entity) to your block for persistent data:

```groovy
import de.luckymcdev.foundryengine.common.builder.blockentity.BlockEntityBuilder

def beBuilder = new BlockEntityBuilder<MyBlockEntity>(id("my_be"), MyBlockEntity::new)
    .hasTick()                                // Enable per-tick updates

BlockBuilder.create(id("my_block"))
    .blockEntity(beBuilder)
```

Register the block entity alongside the block in `BundleEvents.registry`.

## Block tags

Attach block and item tags:

```groovy
import de.luckymcdev.foundryengine.common.builder.tag.BlockTagBuilder

BlockBuilder.create(id("my_block"))
    .tag(BlockTagBuilder.create(Common.id("my_blocks")))
    .tag(Common.id("mineable/pickaxe"))       // Shorthand for existing tags
```

## Ghost blocks

Ghost blocks are blocks that are not really rendered. When using just `.ghost()` it is visible by particle like the barrier block when the block item is held.

```groovy
// Default: visible only when holding the block's item
BlockBuilder.create(id("waypoint_marker"))
    .ghost()
    .properties { it.strength(-1.0f, 3600000.0f).noCollision() }
```

### Custom visibility predicate

```groovy
// Visible only to players holding a diamond
BlockBuilder.create(id("hidden_switch"))
    .ghost { player, state -> player.isHolding(Items.DIAMOND) }
    .properties { it.noCollision() }
```

Ghost blocks use `EngineBlock` internally (so callbacks work) and show floating mini-block particles (`ParticleTypes.BLOCK_MARKER`) when a creative player meeting the visibility condition is nearby.

## Behavior callbacks

Callbacks let your block react to the world:

```groovy
BlockBuilder.create(id("hot_plate"))
    .stepOn { level, pos, onState, entity ->          // Entity walks on it
        if (entity instanceof LivingEntity) {
            entity.hurt(entity.damageSources().hotFloor(), 1.0f)
        }
    }
    .destroy { level, pos, state ->                   // Block is broken
        println "Broken at $pos"
    }
    .setPlacedBy { level, pos, state, by, itemStack -> // Block is placed
        println "Placed by ${by?.name}"
    }
```

### Available callbacks

| Method                    | When it runs                              | Parameters                                                    |
|---------------------------|-------------------------------------------|---------------------------------------------------------------|
| `animateTick(cb)`         | Random display ticks (particles, sounds)  | `BlockState, Level, BlockPos, RandomSource`                   |
| `destroy(cb)`             | Block is broken                           | `LevelAccessor, BlockPos, BlockState`                         |
| `wasExploded(cb)`         | Block is destroyed by explosion           | `ServerLevel, BlockPos, Explosion`                            |
| `stepOn(cb)`              | Entity walks on the block                 | `Level, BlockPos, BlockState, Entity`                         |
| `setPlacedBy(cb)`         | Block is placed by a player               | `Level, BlockPos, BlockState, LivingEntity, ItemStack`        |
| `fallOn(cb)`              | Entity falls onto the block               | `Level, BlockState, BlockPos, Entity, double fallDistance`    |
| `playerWillDestroy(cb)`   | Player starts breaking                    | `Level, BlockPos, BlockState, Player`                         |
| `playerDestroy(cb)`       | Block is harvested by player              | `Level, Player, BlockPos, BlockState, BlockEntity, ItemStack` |
| `handlePrecipitation(cb)` | Block is hit by precipitation (rain/snow) | `BlockState, Level, BlockPos, Biome.Precipitation`            |

## Item callbacks for blocks

Blocks also expose item callbacks that apply to the block's item form:

```groovy
BlockBuilder.create(id("my_block"))
    .itemUse { level, player, hand ->            // Use the block item in air
        InteractionResult.SUCCESS
    }
    .itemUseOn { context ->                      // Place attempt
        InteractionResult.SUCCESS
    }
    .itemInventoryTick { stack, level, owner, slot ->
        // Block item ticking in inventory
    }
```

## Registration

Register blocks inside `BundleEvents.registry`:

```groovy
BundleEvents.registry {
    it.blocks(myBlock, anotherBlock)
}
```

## Next

- [Creating Recipes](creating-recipes.md) — add recipes for your blocks
- [Registration](registration.md) — how registration works
