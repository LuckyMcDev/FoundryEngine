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

| Method                  | When it runs                             | Parameters                                                 |
|-------------------------|------------------------------------------|------------------------------------------------------------|
| `animateTick(cb)`       | Random display ticks (particles, sounds) | `BlockState, Level, BlockPos, RandomSource`                |
| `destroy(cb)`           | Block is broken                          | `LevelAccessor, BlockPos, BlockState`                      |
| `wasExploded(cb)`       | Block is destroyed by explosion          | `ServerLevel, BlockPos, Explosion`                         |
| `stepOn(cb)`            | Entity walks on the block                | `Level, BlockPos, BlockState, Entity`                      |
| `setPlacedBy(cb)`       | Block is placed by a player              | `Level, BlockPos, BlockState, LivingEntity, ItemStack`     |
| `fallOn(cb)`            | Entity falls onto the block              | `Level, BlockState, BlockPos, Entity, double fallDistance` |
| `playerWillDestroy(cb)` | Player starts breaking                   | `Level, BlockPos, BlockState, Player`                      |

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
