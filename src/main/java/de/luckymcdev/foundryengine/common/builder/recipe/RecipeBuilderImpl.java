package de.luckymcdev.foundryengine.common.builder.recipe;

import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeResult;
import de.luckymcdev.foundryengine.common.builder.BuilderState;
import de.luckymcdev.foundryengine.common.registry.EngineRegistries;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.*;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.crafting.JShapedRecipe;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.crafting.JShapelessRecipe;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.smelting.JSmeltingRecipe;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.smelting.SmeltingTypes;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.smithing.JSmithingTransformRecipe;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.smithing.JSmithingTrimRecipe;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Recipe Builder using composition instead of inheritance.
 * Simpler and more flexible than extending BuilderBaseImpl.
 */
public class RecipeBuilderImpl implements RecipeBuilder {
    private final BuilderState<RecipeResult> state;
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
    private Ingredient smithingBase;
    private Ingredient smithingAddition;
    private Ingredient smithingTemplate;
    private float experience = 0.1f;
    private int cookingTime = 200;
    private String group = "";

    private RecipeBuilderImpl(Identifier id, RecipeType type, @Nullable ItemLike result) {
        this.state = new BuilderState<>(id);
        this.state.registryKey = EngineRegistries.Keys.RECIPES;
        this.type = type;
        this.result = result;
    }

    public static RecipeBuilderImpl shaped(Identifier id, ItemLike result) {
        return new RecipeBuilderImpl(id, RecipeType.SHAPED, result);
    }

    public static RecipeBuilderImpl shapeless(Identifier id, ItemLike result) {
        return new RecipeBuilderImpl(id, RecipeType.SHAPELESS, result);
    }

    public static RecipeBuilderImpl smelting(Identifier id, ItemLike result) {
        return new RecipeBuilderImpl(id, RecipeType.SMELTING, result);
    }

    public static RecipeBuilderImpl blasting(Identifier id, ItemLike result) {
        return new RecipeBuilderImpl(id, RecipeType.BLASTING, result);
    }

    public static RecipeBuilderImpl smoking(Identifier id, ItemLike result) {
        return new RecipeBuilderImpl(id, RecipeType.SMOKING, result);
    }

    public static RecipeBuilderImpl campfireCooking(Identifier id, ItemLike result) {
        return new RecipeBuilderImpl(id, RecipeType.CAMPFIRE_COOKING, result);
    }

    public static RecipeBuilderImpl stonecutting(Identifier id, ItemLike result) {
        return new RecipeBuilderImpl(id, RecipeType.STONECUTTING, result);
    }

    public static RecipeBuilderImpl smithingTransform(Identifier id, ItemLike result) {
        return new RecipeBuilderImpl(id, RecipeType.SMITHING_TRANSFORM, result);
    }

    public static RecipeBuilderImpl smithingTrim(Identifier id) {
        return new RecipeBuilderImpl(id, RecipeType.SMITHING_TRIM, null);
    }

    @Override
    public RecipeBuilder withItemGetter(HolderGetter<Item> items) {
        this.items = items;
        return this;
    }

    @Override
    public RecipeBuilder count(int count) {
        this.resultCount = count;
        return this;
    }

    @Override
    public RecipeBuilder category(RecipeCategory category) {
        this.category = category;
        return this;
    }

    @Override
    public RecipeBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public Identifier getRecipeId() {
        return state.id;
    }

    public RecipeType getRecipeType() {
        return type;
    }

    @Override
    public ItemLike getResultItem() {
        return result;
    }

    @Override
    public int getResultCount() {
        return resultCount;
    }

    @Override
    public RecipeBuilder pattern(String... pattern) {
        if (type != RecipeType.SHAPED) {
            throw new IllegalStateException("Pattern can only be set for shaped recipes");
        }
        this.pattern.addAll(Arrays.asList(pattern));
        return this;
    }

    @Override
    public RecipeBuilder define(char symbol, ItemLike item) {
        return define(symbol, Ingredient.of(item));
    }

    @Override
    public RecipeBuilder define(char symbol, Ingredient ingredient) {
        if (type != RecipeType.SHAPED) {
            throw new IllegalStateException("Define can only be used for shaped recipes");
        }
        this.keys.put(symbol, ingredient);
        return this;
    }

    @Override
    public RecipeBuilder requires(ItemLike item) {
        return requires(item, 1);
    }

    @Override
    public RecipeBuilder requires(ItemLike item, int count) {
        return requires(Ingredient.of(item), count);
    }

    @Override
    public RecipeBuilder requires(Ingredient ingredient) {
        return requires(ingredient, 1);
    }

    @Override
    public RecipeBuilder requires(Ingredient ingredient, int count) {
        if (type != RecipeType.SHAPELESS) {
            throw new IllegalStateException("Requires can only be used for shapeless recipes");
        }
        for (int i = 0; i < count; i++) {
            this.ingredients.add(ingredient);
        }
        return this;
    }

    @Override
    public RecipeBuilder ingredient(ItemLike item) {
        return ingredient(Ingredient.of(item));
    }

    @Override
    public RecipeBuilder ingredient(Ingredient ingredient) {
        if (!isCookingRecipe() && type != RecipeType.STONECUTTING) {
            throw new IllegalStateException("Ingredient can only be set for cooking or stonecutting recipes");
        }
        this.cookingIngredient = ingredient;
        return this;
    }

    @Override
    public RecipeBuilder base(ItemLike item) {
        return base(Ingredient.of(item));
    }

    @Override
    public RecipeBuilder base(Ingredient ingredient) {
        if (!isSmithingRecipe()) {
            throw new IllegalStateException("Base can only be set for smithing recipes");
        }
        this.smithingBase = ingredient;
        return this;
    }

    @Override
    public RecipeBuilder addition(ItemLike item) {
        return addition(Ingredient.of(item));
    }

    @Override
    public RecipeBuilder addition(Ingredient ingredient) {
        if (!isSmithingRecipe()) {
            throw new IllegalStateException("Addition can only be set for smithing recipes");
        }
        this.smithingAddition = ingredient;
        return this;
    }

    @Override
    public RecipeBuilder template(ItemLike item) {
        return template(Ingredient.of(item));
    }

    @Override
    public RecipeBuilder template(Ingredient ingredient) {
        if (!isSmithingRecipe()) {
            throw new IllegalStateException("Template can only be set for smithing recipes");
        }
        this.smithingTemplate = ingredient;
        return this;
    }

    @Override
    public RecipeBuilder experience(float experience) {
        if (!isCookingRecipe()) {
            throw new IllegalStateException("Experience can only be set for cooking recipes");
        }
        this.experience = experience;
        return this;
    }

    @Override
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

    private boolean isSmithingRecipe() {
        return type == RecipeType.SMITHING_TRANSFORM || type == RecipeType.SMITHING_TRIM;
    }

    public AbstractJRecipe toJson() {
        ensureValidForJson(state.id);

        return switch (type) {
            case SHAPED -> buildShapedJson();
            case SHAPELESS -> buildShapelessJson();
            case SMELTING -> buildSmeltingJson(SmeltingTypes.SMELTING);
            case BLASTING -> buildSmeltingJson(SmeltingTypes.BLASTING);
            case SMOKING -> buildSmeltingJson(SmeltingTypes.SMOKING);
            case CAMPFIRE_COOKING -> buildSmeltingJson(SmeltingTypes.CAMPFIRE_COOKING);
            case STONECUTTING -> buildStonecuttingJson();
            case SMITHING_TRANSFORM -> buildSmithingTransformJson();
            case SMITHING_TRIM -> buildSmithingTrimJson();
        };
    }

    private JShapedRecipe buildShapedJson() {
        JShapedRecipe recipe = new JShapedRecipe();
        for (int i = 0; i < pattern.size() && i < 3; i++) {
            recipe.row(i, pattern.get(i));
        }
        for (Map.Entry<Character, Ingredient> entry : keys.entrySet()) {
            recipe.key(String.valueOf(entry.getKey()), ingredientToJson(entry.getValue()));
        }
        recipe.result(createResult());
        if (!group.isEmpty()) {
            recipe.group(group);
        }
        return recipe;
    }

    private JShapelessRecipe buildShapelessJson() {
        JShapelessRecipe recipe = new JShapelessRecipe();
        for (Ingredient ingredient : ingredients) {
            recipe.ingredient(ingredientToJson(ingredient));
        }
        recipe.result(createResult());
        if (!group.isEmpty()) {
            recipe.group(group);
        }
        return recipe;
    }

    private JSmeltingRecipe buildSmeltingJson(String recipeType) {
        JSmeltingRecipe recipe = new JSmeltingRecipe(recipeType);
        recipe.ingredient(ingredientToJson(cookingIngredient));
        recipe.result(createResult());
        recipe.experience((int) experience);
        recipe.cookingTime(cookingTime);
        recipe.category(categoryToString(category));
        if (!group.isEmpty()) {
            recipe.group(group);
        }
        return recipe;
    }

    private JStonecuttingRecipe buildStonecuttingJson() {
        JStonecuttingRecipe recipe = new JStonecuttingRecipe();
        recipe.ingredient(ingredientToJson(cookingIngredient));
        recipe.result(createResult());
        return recipe;
    }

    private JSmithingTransformRecipe buildSmithingTransformJson() {
        JSmithingTransformRecipe recipe = new JSmithingTransformRecipe();
        if (smithingTemplate != null) {
            recipe.template(ingredientToJson(smithingTemplate));
        }
        if (smithingBase != null) {
            recipe.base(ingredientToJson(smithingBase));
        }
        if (smithingAddition != null) {
            recipe.addition(ingredientToJson(smithingAddition));
        }
        recipe.result(createResult());
        return recipe;
    }

    private JSmithingTrimRecipe buildSmithingTrimJson() {
        JSmithingTrimRecipe recipe = new JSmithingTrimRecipe();
        if (smithingTemplate != null) {
            recipe.template(ingredientToJson(smithingTemplate));
        }
        if (smithingBase != null) {
            recipe.base(ingredientToJson(smithingBase));
        } else {
            recipe.trimmableArmor();
        }
        if (smithingAddition != null) {
            recipe.addition(ingredientToJson(smithingAddition));
        }
        return recipe;
    }

    private JResult createResult() {
        JResult jResult = new JResult();
        Identifier itemId = BuiltInRegistries.ITEM.getKey(result.asItem());
        jResult.id(itemId);
        jResult.count(Math.max(1, resultCount));
        return jResult;
    }

    private JIngredient ingredientToJson(Ingredient ingredient) {
        JIngredient jIngredient = new JIngredient();
        var values = ingredient.getValues();
        for (var value : values) {
            value.unwrap().ifLeft(resourceKeyItem -> {
                jIngredient.entry(resourceKeyItem.identifier().toString());
            }).ifRight(item -> {
                Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                jIngredient.entry(itemId.toString());
            });
        }
        return jIngredient;
    }

    private String categoryToString(RecipeCategory category) {
        return switch (category) {
            case BUILDING_BLOCKS -> JRecipeBookCategory.BUILDING;
            case REDSTONE -> JRecipeBookCategory.REDSTONE;
            default -> JRecipeBookCategory.MISCELLANEOUS;
        };
    }

    @Override
    public RecipeResult build() {
        AbstractJRecipe jsonRecipe = toJson();
        return new RecipeResult(state.id, jsonRecipe);
    }

    @Override
    public RecipeResult register(RegisterEvent.RegisterHelper<RecipeResult> helper) {
        RecipeResult result = build();
        helper.register(state.id, result);
        state.setObject(result);
        return result;
    }

    private void ensureValidForJson(Identifier recipeId) {
        if (type == RecipeType.SHAPED) {
            if (pattern.isEmpty()) {
                throw new IllegalStateException("Shaped recipe " + recipeId + " must have a pattern");
            }
            if (keys.isEmpty()) {
                throw new IllegalStateException("Shaped recipe " + recipeId + " must define ingredients");
            }
        } else if (type == RecipeType.SHAPELESS) {
            if (ingredients.isEmpty()) {
                throw new IllegalStateException("Shapeless recipe " + recipeId + " must have ingredients");
            }
        } else if (isCookingRecipe() || type == RecipeType.STONECUTTING) {
            if (cookingIngredient == null) {
                throw new IllegalStateException(type + " recipe " + recipeId + " must have an ingredient");
            }
        } else if (isSmithingRecipe()) {
            if (smithingTemplate == null && smithingBase == null && smithingAddition == null) {
                throw new IllegalStateException("Smithing recipe " + recipeId + " must have template, base, and/or addition");
            }
        }
    }

    @Override
    public RecipeResult get() {
        return state.get();
    }

    @Override
    public RecipeResult getOrCreate() {
        return state.getOrCreate();
    }

    @Override
    public Identifier newID(String pre, String post) {
        return state.newID(pre, post);
    }

    public enum RecipeType {
        SHAPED,
        SHAPELESS,
        SMELTING,
        BLASTING,
        SMOKING,
        CAMPFIRE_COOKING,
        STONECUTTING,
        SMITHING_TRANSFORM,
        SMITHING_TRIM
    }
}