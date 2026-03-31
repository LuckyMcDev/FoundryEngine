# Recipe Builder

The `RecipeBuilder` is a utility class designed to simplify the creation of recipes using a fluent builder pattern. It
supports various recipe types, including shaped, shapeless, smelting, blasting, smoking, campfire cooking, stonecutting,
and smithing.

## Usage

To start, create a new instance using a unique `Identifier` and specify the recipe type.

```java{3}
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder;

RecipeBuilder recipe = RecipeBuilder.shaped(Identifier.fromNamespaceAndPath("bundleid", "example_recipe"), Items.DIAMOND);
```

## Properties

The `RecipeBuilder` allows you to configure various properties of the recipe, such as the result count, category, group,
and unlocking criteria.

### Result Count

Set the number of items produced by the recipe.

```java
recipe.count(4);
```

### Category

Set the recipe category for organization in the recipe book.

```java
recipe.category(RecipeCategory.BUILDING_BLOCKS);
```

```java
recipe.group("example_group");
```

### Unlocking Criteria

Add an advancement criterion that must be met to unlock the recipe.

```java
recipe.unlockedBy("has_diamond",InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND));
```

## Recipe-Specific Configuration

Depending on the recipe type, you can configure additional properties.

### Shaped Recipes

Define the crafting pattern and ingredients.

```java
recipe.pattern("###","# #","###");
recipe.

define('#',Items.DIAMOND);
```

### Shapeless Recipes

Add required ingredients.

```java
recipe.requires(Items.DIAMOND, 3);
```

### Cooking Recipes

Set the input ingredient, experience, and cooking time.

```java
recipe.ingredient(Items.RAW_IRON);
recipe.

experience(0.7f);
recipe.

cookingTime(200);
```

### Smithing Recipes

Set the base, addition, and template ingredients.

```java
recipe.base(Items.IRON_SWORD);
recipe.

addition(Items.DIAMOND);
recipe.

template(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
```

## Registration

To register your recipes, listen for the `RegistryEvent` on the bundle bus.

```java{4,7}
public static void onRegister(RegistryEvent event) {

    // Single registration
    event.recipes(recipe);

    // Bulk registration
    event.recipes(recipe1, recipe2, recipe3);
}
```

::: tip Organization
You can call `event.recipes()` multiple times or pass an infinite number of arguments in a single call. Choose the style
that best fits your project's organization.
:::
