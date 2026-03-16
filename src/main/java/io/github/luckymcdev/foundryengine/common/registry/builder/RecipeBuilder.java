package io.github.luckymcdev.foundryengine.common.registry.builder;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.*;

public class RecipeBuilder extends BuilderBase<Recipe<?>> {
    private final RecipeType type;
    private final ItemLike result;
    private final List<String> pattern = new ArrayList<>();
    private final Map<Character, Ingredient> keys = new HashMap<>();
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private int resultCount = 1;
    private RecipeCategory category = RecipeCategory.MISC;
    private HolderGetter<Item> items;
    private Ingredient cookingIngredient;
    private float experience = 0.1f;
    private int cookingTime = 200;
    private String group = "";

    private RecipeBuilder(Identifier id, RecipeType type, ItemLike result) {
        super(id);
        this.registryKey = Registries.RECIPE;
        this.type = type;
        this.result = result;
    }

    public static RecipeBuilder shaped(Identifier id, ItemLike result) {
        return new RecipeBuilder(id, RecipeType.SHAPED, result);
    }

    public static RecipeBuilder shapeless(Identifier id, ItemLike result) {
        return new RecipeBuilder(id, RecipeType.SHAPELESS, result);
    }

    public static RecipeBuilder smelting(Identifier id, ItemLike result) {
        return new RecipeBuilder(id, RecipeType.SMELTING, result);
    }

    public static RecipeBuilder blasting(Identifier id, ItemLike result) {
        return new RecipeBuilder(id, RecipeType.BLASTING, result);
    }

    public static RecipeBuilder smoking(Identifier id, ItemLike result) {
        return new RecipeBuilder(id, RecipeType.SMOKING, result);
    }

    public static RecipeBuilder campfireCooking(Identifier id, ItemLike result) {
        return new RecipeBuilder(id, RecipeType.CAMPFIRE_COOKING, result);
    }

    public RecipeBuilder withItemGetter(HolderGetter<Item> items) {
        this.items = items;
        return this;
    }

    public RecipeBuilder count(int count) {
        this.resultCount = count;
        return this;
    }

    public RecipeBuilder category(RecipeCategory category) {
        this.category = category;
        return this;
    }

    public RecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public Identifier getRecipeId() {
        return id;
    }

    public RecipeType getRecipeType() {
        return type;
    }

    public ItemLike getResultItem() {
        return result;
    }

    public int getResultCount() {
        return resultCount;
    }

    public RecipeBuilder pattern(String... pattern) {
        if (type != RecipeType.SHAPED) {
            throw new IllegalStateException("Pattern can only be set for shaped recipes");
        }
        this.pattern.addAll(Arrays.asList(pattern));
        return this;
    }

    public RecipeBuilder define(char symbol, ItemLike item) {
        return define(symbol, Ingredient.of(item));
    }

    public RecipeBuilder define(char symbol, Ingredient ingredient) {
        if (type != RecipeType.SHAPED) {
            throw new IllegalStateException("Define can only be used for shaped recipes");
        }
        this.keys.put(symbol, ingredient);
        return this;
    }

    public RecipeBuilder requires(ItemLike item) {
        return requires(item, 1);
    }

    public RecipeBuilder requires(ItemLike item, int count) {
        return requires(Ingredient.of(item), count);
    }

    public RecipeBuilder requires(Ingredient ingredient) {
        return requires(ingredient, 1);
    }

    public RecipeBuilder requires(Ingredient ingredient, int count) {
        if (type != RecipeType.SHAPELESS) {
            throw new IllegalStateException("Requires can only be used for shapeless recipes");
        }
        for (int i = 0; i < count; i++) {
            this.ingredients.add(ingredient);
        }
        return this;
    }

    public RecipeBuilder ingredient(ItemLike item) {
        return ingredient(Ingredient.of(item));
    }

    public RecipeBuilder ingredient(Ingredient ingredient) {
        if (!isCookingRecipe()) {
            throw new IllegalStateException("Ingredient can only be set for cooking recipes");
        }
        this.cookingIngredient = ingredient;
        return this;
    }

    public RecipeBuilder experience(float experience) {
        if (!isCookingRecipe()) {
            throw new IllegalStateException("Experience can only be set for cooking recipes");
        }
        this.experience = experience;
        return this;
    }

    public RecipeBuilder cookingTime(int ticks) {
        if (!isCookingRecipe()) {
            throw new IllegalStateException("Cooking time can only be set for cooking recipes");
        }
        this.cookingTime = ticks;
        return this;
    }

    private boolean isCookingRecipe() {
        return type == RecipeType.SMELTING ||
                type == RecipeType.BLASTING ||
                type == RecipeType.SMOKING ||
                type == RecipeType.CAMPFIRE_COOKING;
    }

    public void save(RecipeOutput output) {
        save(output, id);
    }

    public void save(RecipeOutput output, Identifier customId) {
        save(output, ResourceKey.create(Registries.RECIPE, customId));
    }

    public void save(RecipeOutput output, ResourceKey<Recipe<?>> resourceKey) {
        ensureValid(resourceKey);

        switch (type) {
            case SHAPED -> buildShaped(output, resourceKey);
            case SHAPELESS -> buildShapeless(output, resourceKey);
            case SMELTING -> buildCooking(output, resourceKey, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new);
            case BLASTING -> buildCooking(output, resourceKey, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new);
            case SMOKING -> buildCooking(output, resourceKey, RecipeSerializer.SMOKING_RECIPE, SmokingRecipe::new);
            case CAMPFIRE_COOKING ->
                    buildCooking(output, resourceKey, RecipeSerializer.CAMPFIRE_COOKING_RECIPE, CampfireCookingRecipe::new);
        }
    }

    @Override
    public Recipe<?> build() {
        return null;
    }

    private void buildShaped(RecipeOutput output, ResourceKey<Recipe<?>> recipeKey) {
        if (items == null) {
            throw new IllegalStateException("HolderGetter<Item> is required. Call withItemGetter() before save()");
        }

        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(items, category, result, resultCount);

        for (String line : pattern) {
            builder.pattern(line);
        }

        for (Map.Entry<Character, Ingredient> entry : keys.entrySet()) {
            builder.define(entry.getKey(), entry.getValue());
        }

        if (!group.isEmpty()) {
            builder.group(group);
        }

        for (Map.Entry<String, Criterion<?>> entry : criteria.entrySet()) {
            builder.unlockedBy(entry.getKey(), entry.getValue());
        }

        builder.save(output, recipeKey);
    }

    private void buildShapeless(RecipeOutput output, ResourceKey<Recipe<?>> recipeKey) {
        if (items == null) {
            throw new IllegalStateException("HolderGetter<Item> is required. Call withItemGetter() before save()");
        }

        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(items, category, result, resultCount);

        for (Ingredient ingredient : ingredients) {
            builder.requires(ingredient);
        }

        if (!group.isEmpty()) {
            builder.group(group);
        }

        for (Map.Entry<String, Criterion<?>> entry : criteria.entrySet()) {
            builder.unlockedBy(entry.getKey(), entry.getValue());
        }

        builder.save(output, recipeKey);
    }

    private <T extends AbstractCookingRecipe> void buildCooking(
            RecipeOutput output,
            ResourceKey<Recipe<?>> recipeKey,
            RecipeSerializer<T> serializer,
            AbstractCookingRecipe.Factory<T> factory) {

        SimpleCookingRecipeBuilder builder = SimpleCookingRecipeBuilder.generic(
                cookingIngredient,
                category,
                result,
                experience,
                cookingTime,
                serializer,
                factory
        );

        if (!group.isEmpty()) {
            builder.group(group);
        }

        for (Map.Entry<String, Criterion<?>> entry : criteria.entrySet()) {
            builder.unlockedBy(entry.getKey(), entry.getValue());
        }

        builder.save(output, recipeKey);
    }

    public RecipeBuilder register(RegisterEvent.RegisterHelper<RecipeBuilder> helper) {
        helper.register(this.id, this);
        return this;
    }

    private void ensureValid(ResourceKey<Recipe<?>> recipeKey) {
        if (type == RecipeType.SHAPED) {
            if (pattern.isEmpty()) {
                throw new IllegalStateException("Shaped recipe " + recipeKey.identifier() + " must have a pattern");
            }
            if (keys.isEmpty()) {
                throw new IllegalStateException("Shaped recipe " + recipeKey.identifier() + " must define ingredients");
            }
        } else if (type == RecipeType.SHAPELESS) {
            if (ingredients.isEmpty()) {
                throw new IllegalStateException("Shapeless recipe " + recipeKey.identifier() + " must have ingredients");
            }
        } else if (isCookingRecipe()) {
            if (cookingIngredient == null) {
                throw new IllegalStateException("Cooking recipe " + recipeKey.identifier() + " must have an ingredient");
            }
        }

        if (criteria.isEmpty()) {
            throw new IllegalStateException("Recipe " + recipeKey.identifier() + " must have at least one unlock criterion");
        }
    }

    public enum RecipeType {
        SHAPED,
        SHAPELESS,
        SMELTING,
        BLASTING,
        SMOKING,
        CAMPFIRE_COOKING
    }
}