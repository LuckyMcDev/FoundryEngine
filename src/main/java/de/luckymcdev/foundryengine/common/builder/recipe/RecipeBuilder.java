package de.luckymcdev.foundryengine.common.builder.recipe;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class RecipeBuilder extends AbstractBuilder<RecipeResult> {
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

    private RecipeBuilder(Identifier id, RecipeType type, @Nullable ItemLike result) {
        super(id);
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

    public static RecipeBuilder stonecutting(Identifier id, ItemLike result) {
        return new RecipeBuilder(id, RecipeType.STONECUTTING, result);
    }

    public static RecipeBuilder smithingTransform(Identifier id, ItemLike result) {
        return new RecipeBuilder(id, RecipeType.SMITHING_TRANSFORM, result);
    }

    public static RecipeBuilder smithingTrim(Identifier id) {
        return new RecipeBuilder(id, RecipeType.SMITHING_TRIM, null);
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
        if (!isCookingRecipe() && type != RecipeType.STONECUTTING) {
            throw new IllegalStateException("Ingredient can only be set for cooking or stonecutting recipes");
        }
        this.cookingIngredient = ingredient;
        return this;
    }

    public RecipeBuilder base(ItemLike item) {
        return base(Ingredient.of(item));
    }

    public RecipeBuilder base(Ingredient ingredient) {
        if (!isSmithingRecipe()) {
            throw new IllegalStateException("Base can only be set for smithing recipes");
        }
        this.smithingBase = ingredient;
        return this;
    }

    public RecipeBuilder addition(ItemLike item) {
        return addition(Ingredient.of(item));
    }

    public RecipeBuilder addition(Ingredient ingredient) {
        if (!isSmithingRecipe()) {
            throw new IllegalStateException("Addition can only be set for smithing recipes");
        }
        this.smithingAddition = ingredient;
        return this;
    }

    public RecipeBuilder template(ItemLike item) {
        return template(Ingredient.of(item));
    }

    public RecipeBuilder template(Ingredient ingredient) {
        if (!isSmithingRecipe()) {
            throw new IllegalStateException("Template can only be set for smithing recipes");
        }
        this.smithingTemplate = ingredient;
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

    private boolean isSmithingRecipe() {
        return type == RecipeType.SMITHING_TRANSFORM || type == RecipeType.SMITHING_TRIM;
    }

    public RecipeResult build() {
        ensureValid();
        return new RecipeResult(id, this::saveTo);
    }

    public RecipeResult register(RegisterEvent.RegisterHelper<RecipeResult> helper) {
        RecipeResult result = build();
        helper.register(id, result);
        setObject(result);
        return result;
    }

    private void ensureValid() {
        if (type == RecipeType.SHAPED) {
            if (pattern.isEmpty()) {
                throw new IllegalStateException("Shaped recipe " + id + " must have a pattern");
            }
            if (keys.isEmpty()) {
                throw new IllegalStateException("Shaped recipe " + id + " must define ingredients");
            }
        } else if (type == RecipeType.SHAPELESS) {
            if (ingredients.isEmpty()) {
                throw new IllegalStateException("Shapeless recipe " + id + " must have ingredients");
            }
        } else if (isCookingRecipe() || type == RecipeType.STONECUTTING) {
            if (cookingIngredient == null) {
                throw new IllegalStateException(type + " recipe " + id + " must have an ingredient");
            }
        } else if (isSmithingRecipe()) {
            if (smithingTemplate == null && smithingBase == null && smithingAddition == null) {
                throw new IllegalStateException("Smithing recipe " + id + " must have template, base, and/or addition");
            }
        }
    }

    private void saveTo(RecipeOutput output, HolderLookup.Provider registries) {
        switch (type) {
            case SHAPED -> saveShaped(output, registries);
            case SHAPELESS -> saveShapeless(output, registries);
            case SMELTING -> saveCooking(output, registries, true, false, false, false);
            case BLASTING -> saveCooking(output, registries, false, true, false, false);
            case SMOKING -> saveCooking(output, registries, false, false, true, false);
            case CAMPFIRE_COOKING -> saveCooking(output, registries, false, false, false, true);
            case STONECUTTING -> saveStonecutting(output, registries);
            case SMITHING_TRANSFORM -> saveSmithingTransform(output, registries);
            case SMITHING_TRIM -> saveSmithingTrim(output, registries);
        }
    }

    private void saveShaped(RecipeOutput output, HolderLookup.Provider registries) {
        var itemGetter = registries.lookupOrThrow(Registries.ITEM);
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(itemGetter, category, result);
        for (String row : pattern) {
            builder.pattern(row);
        }
        for (Map.Entry<Character, Ingredient> entry : keys.entrySet()) {
            builder.define(entry.getKey(), entry.getValue());
        }
        if (!group.isEmpty()) {
            builder.group(group);
        }
        applyCriteria(builder);
        builder.save(output, id.toString());
    }

    private void saveShapeless(RecipeOutput output, HolderLookup.Provider registries) {
        var itemGetter = registries.lookupOrThrow(Registries.ITEM);
        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(itemGetter, category, result);
        for (Ingredient ingredient : ingredients) {
            builder.requires(ingredient);
        }
        if (!group.isEmpty()) {
            builder.group(group);
        }
        applyCriteria(builder);
        builder.save(output, id.toString());
    }

    private void saveCooking(RecipeOutput output, HolderLookup.Provider registries, boolean smelting, boolean blasting, boolean smoking, boolean campfire) {
        CookingBookCategory bookCategory = CookingBookCategory.MISC;
        SimpleCookingRecipeBuilder builder;
        if (smelting) {
            builder = SimpleCookingRecipeBuilder.smelting(cookingIngredient, category, bookCategory, result, experience, cookingTime);
        } else if (blasting) {
            builder = SimpleCookingRecipeBuilder.blasting(cookingIngredient, category, bookCategory, result, experience, cookingTime);
        } else if (smoking) {
            builder = SimpleCookingRecipeBuilder.smoking(cookingIngredient, category, result, experience, cookingTime);
        } else if (campfire) {
            builder = SimpleCookingRecipeBuilder.campfireCooking(cookingIngredient, category, result, experience, cookingTime);
        } else {
            throw new IllegalStateException("Unknown cooking type");
        }
        if (!group.isEmpty()) {
            builder.group(group);
        }
        applyCriteria(builder);
        builder.save(output, id.toString());
    }

    private void saveStonecutting(RecipeOutput output, HolderLookup.Provider registries) {
        SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(cookingIngredient, category, result, resultCount);
        applyCriteria(builder);
        builder.save(output, id.toString());
    }

    private void saveSmithingTransform(RecipeOutput output, HolderLookup.Provider registries) {
        SmithingTransformRecipeBuilder builder = SmithingTransformRecipeBuilder.smithing(
                smithingTemplate != null ? smithingTemplate : Ingredient.of(),
                smithingBase != null ? smithingBase : Ingredient.of(),
                smithingAddition != null ? smithingAddition : Ingredient.of(),
                category,
                result.asItem()
        );
        applyCriteria(builder);
        builder.save(output, id.toString());
    }

    private void saveSmithingTrim(RecipeOutput output, HolderLookup.Provider registries) {
        throw new UnsupportedOperationException("Smithing trim recipes require specifying a trim pattern, which is not yet supported by this API");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void applyCriteria(Object recipeBuilder) {
        if (!criteria.isEmpty()) {
            for (Map.Entry<String, Criterion<?>> entry : criteria.entrySet()) {
                switch (recipeBuilder) {
                    case ShapedRecipeBuilder shaped -> shaped.unlockedBy(entry.getKey(), entry.getValue());
                    case ShapelessRecipeBuilder shapeless -> shapeless.unlockedBy(entry.getKey(), entry.getValue());
                    case SimpleCookingRecipeBuilder cooking -> cooking.unlockedBy(entry.getKey(), entry.getValue());
                    case SingleItemRecipeBuilder single -> single.unlockedBy(entry.getKey(), entry.getValue());
                    case SmithingTransformRecipeBuilder smithing -> smithing.unlocks(entry.getKey(), entry.getValue());
                    case SmithingTrimRecipeBuilder smithingTrim ->
                            smithingTrim.unlocks(entry.getKey(), entry.getValue());
                    default -> {
                    }
                }
            }
        } else if (result != null) {
            Criterion<?> criterion = InventoryChangeTrigger.TriggerInstance.hasItems(result);
            String name = "has_" + id.getPath().replace('/', '_');
            if (recipeBuilder instanceof ShapedRecipeBuilder shaped) {
                shaped.unlockedBy(name, criterion);
            } else if (recipeBuilder instanceof ShapelessRecipeBuilder shapeless) {
                shapeless.unlockedBy(name, criterion);
            } else if (recipeBuilder instanceof SimpleCookingRecipeBuilder cooking) {
                cooking.unlockedBy(name, criterion);
            } else if (recipeBuilder instanceof SingleItemRecipeBuilder single) {
                single.unlockedBy(name, criterion);
            } else if (recipeBuilder instanceof SmithingTransformRecipeBuilder smithing) {
                smithing.unlocks(name, criterion);
            } else if (recipeBuilder instanceof SmithingTrimRecipeBuilder smithingTrim) {
                smithingTrim.unlocks(name, criterion);
            }
        }
    }

    public RecipeBuilder generateData(boolean generate) {
        this.generateData = generate;
        return this;
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
