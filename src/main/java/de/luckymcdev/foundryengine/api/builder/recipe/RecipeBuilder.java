package de.luckymcdev.foundryengine.api.builder.recipe;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.common.registry.builder.recipe.RecipeBuilderImpl;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Builder interface for creating recipes.
 * Provides a fluent API for recipe registration with different recipe types.
 * <p>
 * Use the static factory methods to create builders for specific recipe types:
 * shaped, shapeless, smelting, blasting, smoking, campfireCooking, stonecutting,
 * smithingTransform, and smithingTrim.
 */
public interface RecipeBuilder extends BuilderBase<RecipeResult> {

    /**
     * Creates a shaped crafting recipe builder.
     *
     * @param id     The recipe identifier
     * @param result The item to produce
     * @return A new RecipeBuilder for shaped recipes
     */
    static RecipeBuilder shaped(Identifier id, ItemLike result) {
        return RecipeBuilderImpl.shaped(id, result);
    }

    /**
     * Creates a shapeless crafting recipe builder.
     *
     * @param id     The recipe identifier
     * @param result The item to produce
     * @return A new RecipeBuilder for shapeless recipes
     */
    static RecipeBuilder shapeless(Identifier id, ItemLike result) {
        return RecipeBuilderImpl.shapeless(id, result);
    }

    /**
     * Creates a smelting recipe builder.
     *
     * @param id     The recipe identifier
     * @param result The item to produce
     * @return A new RecipeBuilder for smelting recipes
     */
    static RecipeBuilder smelting(Identifier id, ItemLike result) {
        return RecipeBuilderImpl.smelting(id, result);
    }

    /**
     * Creates a blasting recipe builder.
     *
     * @param id     The recipe identifier
     * @param result The item to produce
     * @return A new RecipeBuilder for blasting recipes
     */
    static RecipeBuilder blasting(Identifier id, ItemLike result) {
        return RecipeBuilderImpl.blasting(id, result);
    }

    /**
     * Creates a smoking recipe builder.
     *
     * @param id     The recipe identifier
     * @param result The item to produce
     * @return A new RecipeBuilder for smoking recipes
     */
    static RecipeBuilder smoking(Identifier id, ItemLike result) {
        return RecipeBuilderImpl.smoking(id, result);
    }

    /**
     * Creates a campfire cooking recipe builder.
     *
     * @param id     The recipe identifier
     * @param result The item to produce
     * @return A new RecipeBuilder for campfire cooking recipes
     */
    static RecipeBuilder campfireCooking(Identifier id, ItemLike result) {
        return RecipeBuilderImpl.campfireCooking(id, result);
    }

    /**
     * Creates a stonecutting recipe builder.
     *
     * @param id     The recipe identifier
     * @param result The item to produce
     * @return A new RecipeBuilder for stonecutting recipes
     */
    static RecipeBuilder stonecutting(Identifier id, ItemLike result) {
        return RecipeBuilderImpl.stonecutting(id, result);
    }

    /**
     * Creates a smithing transform recipe builder.
     *
     * @param id     The recipe identifier
     * @param result The item to produce
     * @return A new RecipeBuilder for smithing transform recipes
     */
    static RecipeBuilder smithingTransform(Identifier id, ItemLike result) {
        return RecipeBuilderImpl.smithingTransform(id, result);
    }

    /**
     * Creates a smithing trim recipe builder.
     *
     * @param id The recipe identifier
     * @return A new RecipeBuilder for smithing trim recipes
     */
    static RecipeBuilder smithingTrim(Identifier id) {
        return RecipeBuilderImpl.smithingTrim(id);
    }

    /**
     * Sets the item holder getter for resolving items.
     *
     * @param items The HolderGetter for items
     * @return This builder for chaining
     */
    RecipeBuilder withItemGetter(HolderGetter<Item> items);

    /**
     * Sets the number of items produced by this recipe.
     *
     * @param count The result count
     * @return This builder for chaining
     */
    RecipeBuilder count(int count);

    /**
     * Sets the recipe category for organization in the recipe book.
     *
     * @param category The recipe category
     * @return This builder for chaining
     */
    RecipeBuilder category(RecipeCategory category);

    /**
     * Sets the recipe group for organization in the recipe book.
     *
     * @param group The group name
     * @return This builder for chaining
     */
    RecipeBuilder group(String group);

    /**
     * Adds an advancement criterion that must be met to unlock this recipe.
     *
     * @param name      The criterion name
     * @param criterion The criterion
     * @return This builder for chaining
     */
    RecipeBuilder unlockedBy(String name, Criterion<?> criterion);

    /**
     * Gets the recipe ID.
     *
     * @return The recipe identifier
     */
    Identifier getRecipeId();

    /**
     * Gets the result item for this recipe.
     *
     * @return The result item
     */
    ItemLike getResultItem();

    /**
     * Gets the result count for this recipe.
     *
     * @return The number of items produced
     */
    int getResultCount();

    /**
     * Defines the crafting pattern for shaped recipes.
     * Each string represents a row in the crafting grid.
     *
     * @param pattern The pattern rows
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-shaped recipe
     */
    RecipeBuilder pattern(String... pattern);

    /**
     * Defines what item a symbol in the pattern represents.
     *
     * @param symbol The character used in the pattern
     * @param item   The item this symbol represents
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-shaped recipe
     */
    RecipeBuilder define(char symbol, ItemLike item);

    /**
     * Defines what ingredient a symbol in the pattern represents.
     *
     * @param symbol     The character used in the pattern
     * @param ingredient The ingredient this symbol represents
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-shaped recipe
     */
    RecipeBuilder define(char symbol, Ingredient ingredient);

    /**
     * Adds a required item for shapeless recipes.
     *
     * @param item The required item
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-shapeless recipe
     */
    RecipeBuilder requires(ItemLike item);

    /**
     * Adds required items for shapeless recipes.
     *
     * @param item  The required item
     * @param count How many of this item are required
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-shapeless recipe
     */
    RecipeBuilder requires(ItemLike item, int count);

    /**
     * Adds a required ingredient for shapeless recipes.
     *
     * @param ingredient The required ingredient
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-shapeless recipe
     */
    RecipeBuilder requires(Ingredient ingredient);

    /**
     * Adds required ingredients for shapeless recipes.
     *
     * @param ingredient The required ingredient
     * @param count      How many of this ingredient are required
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-shapeless recipe
     */
    RecipeBuilder requires(Ingredient ingredient, int count);

    /**
     * Sets the input ingredient for cooking or stonecutting recipes.
     *
     * @param item The input item
     * @return This builder for chaining
     * @throws IllegalStateException if called on an incompatible recipe type
     */
    RecipeBuilder ingredient(ItemLike item);

    /**
     * Sets the input ingredient for cooking or stonecutting recipes.
     *
     * @param ingredient The input ingredient
     * @return This builder for chaining
     * @throws IllegalStateException if called on an incompatible recipe type
     */
    RecipeBuilder ingredient(Ingredient ingredient);

    /**
     * Sets the experience gained from cooking recipes.
     *
     * @param experience The experience amount
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-cooking recipe
     */
    RecipeBuilder experience(float experience);

    /**
     * Sets the cooking time in ticks for cooking recipes.
     *
     * @param ticks The cooking time (20 ticks = 1 second)
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-cooking recipe
     */
    RecipeBuilder cookingTime(int ticks);

    /**
     * Sets the base item for smithing recipes.
     *
     * @param item The base item
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-smithing recipe
     */
    RecipeBuilder base(ItemLike item);

    /**
     * Sets the base ingredient for smithing recipes.
     *
     * @param ingredient The base ingredient
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-smithing recipe
     */
    RecipeBuilder base(Ingredient ingredient);

    /**
     * Sets the addition item for smithing recipes.
     *
     * @param item The addition item
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-smithing recipe
     */
    RecipeBuilder addition(ItemLike item);

    /**
     * Sets the addition ingredient for smithing recipes.
     *
     * @param ingredient The addition ingredient
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-smithing recipe
     */
    RecipeBuilder addition(Ingredient ingredient);

    /**
     * Sets the template item for smithing recipes.
     *
     * @param item The template item
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-smithing recipe
     */
    RecipeBuilder template(ItemLike item);

    /**
     * Sets the template ingredient for smithing recipes.
     *
     * @param ingredient The template ingredient
     * @return This builder for chaining
     * @throws IllegalStateException if called on a non-smithing recipe
     */
    RecipeBuilder template(Ingredient ingredient);

    /**
     * Registers this recipe using the provided helper.
     *
     * @param helper The register event helper
     * @return The registered RecipeResult instance
     */
    RecipeResult register(RegisterEvent.RegisterHelper<RecipeResult> helper);
}