# Creating Recipes

FoundryEngine supports all 9 recipe types through `RecipeBuilder` (in `de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder`).

## Crafting recipes

### Shaped (pattern-based)

```groovy
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder

RecipeBuilder.shaped(id("my_sword"), Items.DIAMOND_SWORD)
    .pattern(" D ", " D ", " S ")
    .define('D' as char, Items.DIAMOND)
    .define('S' as char, Items.STICK)
    .category(RecipeCategory.COMBAT)
    .unlockedBy("has_diamond",
        InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
```

### Shapeless (ingredient list, no pattern)

```groovy
RecipeBuilder.shapeless(id("my_item"), Items.FLINT_AND_STEEL)
    .requires(Items.IRON_INGOT)
    .requires(Items.FLINT)
    .unlockedBy("has_iron",
        InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
```

## Cooking recipes

### Smelting

```groovy
RecipeBuilder.smelting(id("my_smelting"), Items.IRON_INGOT)
    .ingredient(Items.IRON_ORE)
    .experience(0.7f)
    .cookingTime(200)
```

### Blasting, Smoking, Campfire

```groovy
RecipeBuilder.blasting(id("my_blasting"), Items.IRON_INGOT)
    .ingredient(Items.RAW_IRON).experience(0.7f).cookingTime(100)

RecipeBuilder.smoking(id("my_smoking"), Items.COOKED_BEEF)
    .ingredient(Items.BEEF).experience(0.35f).cookingTime(100)

RecipeBuilder.campfireCooking(id("my_campfire"), Items.COOKED_BEEF)
    .ingredient(Items.BEEF).experience(0.35f).cookingTime(600)
```

## Other recipe types

### Stonecutting

```groovy
RecipeBuilder.stonecutting(id("my_stonecutting"), Items.STONE_SLAB)
    .ingredient(Items.STONE)
    .count(2)
```

### Smithing

```groovy
RecipeBuilder.smithingTransform(id("my_smithing"), Items.NETHERITE_SWORD)
    .base(Items.DIAMOND_SWORD)
    .addition(Items.NETHERITE_INGOT)
    .template(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
    .unlockedBy("has_netherite",
        InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_INGOT))
```

## Common methods

All recipe types share these methods:

| Method                          | What it does                                    |
|---------------------------------|-------------------------------------------------|
| `count(int)`                    | How many items the recipe produces (default: 1) |
| `category(RecipeCategory)`      | Recipe category for the recipe book             |
| `group(String)`                 | Recipe group for the recipe book                |
| `unlockedBy(String, Criterion)` | What the player needs to unlock this recipe     |

## Next

- [Creating Items](creating-items.md) — items you can use in recipes
- [Creating Blocks](creating-blocks.md) — blocks in recipes
