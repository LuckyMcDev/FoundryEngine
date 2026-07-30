# Creating Items

Items are the most common thing you will create in a bundle. FoundryEngine provides `ItemBuilder` (in `de.luckymcdev.foundryengine.common.builder.item.ItemBuilder`) for this.

## Basic item

```groovy
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder

def myItem = ItemBuilder.create(id("my_item"))
    .stacksTo(16)
```

## Adding properties

```groovy
ItemBuilder.create(id("my_item"))
    .fireResistant()                              // Immune to fire/lava
    .stacksTo(16)                                 // Max stack size
    .component(DataComponents.RARITY, Rarity.UNCOMMON)    // Blue name
    .component(DataComponents.LORE, new ItemLore(List.of(  // Tooltip text
        Component.literal("A legendary blade,"),
        Component.literal("forged in the deeps.")
    )))
```

## Adding behavior with callbacks

Callbacks let you add code that runs when something happens with the item:

```groovy
ItemBuilder.create(id("wand"))
    .use { level, player, hand ->                    // Right-click in air
        player.sendSystemMessage(Component.literal("Whoosh!"))
        return InteractionResult.SUCCESS
    }
    .useOn { context ->                              // Right-click on a block
        println "Used on ${context.pos}"
        return InteractionResult.SUCCESS
    }
    .inventoryTick { stack, level, owner, slot ->    // Every tick in inventory
        if (level.gameTime % 20 == 0) {
            // Per-second effect
        }
    }
    .hurtEnemy { stack, mob, attacker ->             // Hit a mob
        mob.setSecondsOnFire(3)
    }
```

### Available callbacks

| Method              | When it runs            | Parameters                                           | Returns             |
|---------------------|-------------------------|------------------------------------------------------|---------------------|
| `use(cb)`           | Right-click in air      | `Level, Player, InteractionHand`                     | `InteractionResult` |
| `useOn(cb)`         | Right-click on a block  | `UseOnContext`                                       | `InteractionResult` |
| `inventoryTick(cb)` | Every tick in inventory | `ItemStack, ServerLevel, Entity, EquipmentSlot`      | `void`              |
| `hurtEnemy(cb)`     | Hit a mob               | `ItemStack, LivingEntity mob, LivingEntity attacker` | `void`              |
| `postHurtEnemy(cb)` | After hitting a mob     | `ItemStack, LivingEntity mob, LivingEntity attacker` | `void`              |

## Food items

```groovy
ItemBuilder.create(id("cosmic_apple"))
    .component(DataComponents.RARITY, Rarity.EPIC)
    .component(DataComponents.FOOD, new FoodProperties.Builder()
        .nutrition(4).saturationModifier(0.3f).alwaysEdible().build())
    .component(DataComponents.CONSUMABLE, Consumables.defaultFood().build())
    .stacksTo(16)
```

## Additional callbacks

| Method                     | When it runs                   | Parameters                                       | Returns     |
|----------------------------|--------------------------------|--------------------------------------------------|-------------|
| `onUseTick(cb)`            | Each tick while holding use    | `Level, Player, InteractionHand, ItemStack, int` | `void`      |
| `finishUsingItem(cb)`      | After eating/drinking finishes | `ItemStack, Level, LivingEntity`                 | `ItemStack` |
| `releaseUsing(cb)`         | When use is released early     | `ItemStack, Level, LivingEntity, int`            | `boolean`   |
| `onCraftedPostProcess(cb)` | After item is crafted          | `ItemStack, Level`                               | `void`      |

## Item tags

Attach item tags for recipe groups, tool categories, etc.:

```groovy
import de.luckymcdev.foundryengine.common.builder.tag.ItemTagBuilder

ItemBuilder.create(id("my_item"))
    .tag(ItemTagBuilder.create(Common.id("my_items")))
    .tag(Common.id("mineable/sword"))
```

## Registration

Register items inside `BundleEvents.registry`:

```groovy
BundleEvents.registry {
    it.items(myItem, anotherItem)
}
```

## Next

- [Creating Blocks](creating-blocks.md) — add custom blocks
- [Registration](registration.md) — how registration works
