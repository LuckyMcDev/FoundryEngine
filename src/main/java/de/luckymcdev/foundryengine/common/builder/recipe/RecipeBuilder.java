package de.luckymcdev.foundryengine.common.builder.recipe;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
//? if 26.1 {
import net.minecraft.advancements.Criterion;
 //?} else {
/*import net.minecraft.advancements.triggers.Criterion;
*///?}
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.data.recipes.SmithingTrimRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class RecipeBuilder extends AbstractBuilder<BiConsumer<RecipeOutput, HolderLookup.Provider>> {
	private final RecipeType type;
	@Nullable
	private final ItemLike result;
	// Shaped
	private final List<String> pattern = new ArrayList<>();
	private final Map<Character, Ingredient> keys = new LinkedHashMap<>();
	// Shapeless
	private final List<Ingredient> ingredients = new ArrayList<>();
	// Criteria
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
	private int resultCount = 1;
	private RecipeCategory category = RecipeCategory.MISC;
	@Nullable
	private String group;
	// Cooking / stonecutting
	@Nullable
	private Ingredient ingredient;

	// Smithing
	@Nullable
	private Ingredient smithingTemplate;
	@Nullable
	private Ingredient smithingBase;
	@Nullable
	private Ingredient smithingAddition;
	// Trim
	@Nullable
	private ResourceKey<TrimPattern> trimPatternKey;

	// Cooking
	private float experience = 0.1f;
	private int cookingTime = 200;
	// Custom recipe delegate
	@Nullable
	private RecipeSaver customSaver;

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

	public static RecipeBuilder custom(Identifier id, net.minecraft.data.recipes.RecipeBuilder delegate) {
		return custom(id, delegate::save);
	}

	public static RecipeBuilder custom(Identifier id, SmithingTransformRecipeBuilder delegate) {
		return custom(id, delegate::save);
	}

	public static RecipeBuilder custom(Identifier id, SmithingTrimRecipeBuilder delegate) {
		return custom(id, delegate::save);
	}

	private static RecipeBuilder custom(Identifier id, RecipeSaver saver) {
		var builder = new RecipeBuilder(id, RecipeType.CUSTOM, null);
		builder.customSaver = saver;
		return builder;
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
		this.pattern.addAll(List.of(pattern));
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
		this.ingredient = ingredient;
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

	public RecipeBuilder trimPattern(Identifier patternId) {
		if (type != RecipeType.SMITHING_TRIM) {
			throw new IllegalStateException("Trim pattern can only be set for smithing trim recipes");
		}
		this.trimPatternKey = ResourceKey.create(Registries.TRIM_PATTERN, patternId);
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
		return type == RecipeType.SMELTING
			|| type == RecipeType.BLASTING
			|| type == RecipeType.SMOKING
			|| type == RecipeType.CAMPFIRE_COOKING;
	}

	private boolean isSmithingRecipe() {
		return type == RecipeType.SMITHING_TRANSFORM || type == RecipeType.SMITHING_TRIM;
	}

	@Override
	public BiConsumer<RecipeOutput, HolderLookup.Provider> build() {
		ensureValid();
		return (output, registries) -> {
			var itemLookup = registries.lookupOrThrow(Registries.ITEM);
			var key = ResourceKey.create(Registries.RECIPE, id);

			if (customSaver != null) {
				customSaver.save(output, key);
				return;
			}

			switch (type) {
				case SHAPED -> buildShaped(output, key, itemLookup);
				case SHAPELESS -> buildShapeless(output, key, itemLookup);
				case SMELTING, BLASTING, SMOKING, CAMPFIRE_COOKING -> buildCooking(output, key);
				case STONECUTTING -> buildStonecutting(output, key);
				case SMITHING_TRANSFORM -> buildSmithingTransform(output, key);
				case SMITHING_TRIM -> buildSmithingTrim(output, key, registries);
				default -> throw new IllegalStateException("Unhandled recipe type: " + type);
			}
		};
	}

	private void buildShaped(RecipeOutput output, ResourceKey<Recipe<?>> key, HolderGetter<Item> items) {
		var builder = ShapedRecipeBuilder.shaped(items, category, result, resultCount);
		pattern.forEach(builder::pattern);
		keys.forEach(builder::define);
		if (group != null) {
			builder.group(group);
		}
		criteria.forEach(builder::unlockedBy);
		builder.save(output, key);
	}

	private void buildShapeless(RecipeOutput output, ResourceKey<Recipe<?>> key, HolderGetter<Item> items) {
		var builder = ShapelessRecipeBuilder.shapeless(items, category, result, resultCount);
		ingredients.forEach(builder::requires);
		if (group != null) {
			builder.group(group);
		}
		criteria.forEach(builder::unlockedBy);
		builder.save(output, key);
	}

	private void buildCooking(RecipeOutput output, ResourceKey<Recipe<?>> key) {
		if (ingredient == null) {
			throw new IllegalStateException("Cooking recipe missing ingredient");
		}
		var cookingCategory = switch (type) {
			case SMELTING, BLASTING -> CookingBookCategory.MISC;
			case SMOKING, CAMPFIRE_COOKING -> CookingBookCategory.FOOD;
			default -> throw new IllegalStateException("Unexpected cooking type");
		};
		SimpleCookingRecipeBuilder builder;
		switch (type) {
			case SMELTING -> builder = SimpleCookingRecipeBuilder.smelting(ingredient, category, cookingCategory, result, experience, cookingTime);
			case BLASTING -> builder = SimpleCookingRecipeBuilder.blasting(ingredient, category, cookingCategory, result, experience, cookingTime);
			case SMOKING -> builder = SimpleCookingRecipeBuilder.smoking(ingredient, category, result, experience, cookingTime);
			case CAMPFIRE_COOKING -> builder = SimpleCookingRecipeBuilder.campfireCooking(ingredient, category, result, experience, cookingTime);
			default -> throw new IllegalStateException("Unreachable");
		}
		if (group != null) {
			builder.group(group);
		}
		criteria.forEach(builder::unlockedBy);
		builder.save(output, key);
	}

	private void buildStonecutting(RecipeOutput output, ResourceKey<Recipe<?>> key) {
		if (ingredient == null) {
			throw new IllegalStateException("Stonecutting recipe missing ingredient");
		}
		var builder = SingleItemRecipeBuilder.stonecutting(ingredient, category, result, resultCount);
		criteria.forEach(builder::unlockedBy);
		builder.save(output, key);
	}

	private void buildSmithingTransform(RecipeOutput output, ResourceKey<Recipe<?>> key) {
		if (smithingTemplate == null || smithingBase == null || smithingAddition == null || result == null) {
			throw new IllegalStateException("Smithing transform recipe missing required ingredients or result");
		}
		var builder = SmithingTransformRecipeBuilder.smithing(
			smithingTemplate, smithingBase, smithingAddition, category, result.asItem()
		);
		criteria.forEach(builder::unlocks);
		builder.save(output, key);
	}

	private void buildSmithingTrim(RecipeOutput output, ResourceKey<Recipe<?>> key, HolderLookup.Provider registries) {
		if (smithingTemplate == null || smithingBase == null || smithingAddition == null || trimPatternKey == null) {
			throw new IllegalStateException("Smithing trim recipe missing required ingredients or trim pattern");
		}
		var patternHolder = registries.lookupOrThrow(Registries.TRIM_PATTERN)
			.get(trimPatternKey)
			.orElseThrow(() -> new IllegalStateException("Unknown trim pattern: " + trimPatternKey));
		var builder = SmithingTrimRecipeBuilder.smithingTrim(
			smithingTemplate, smithingBase, smithingAddition, patternHolder, category
		);
		criteria.forEach(builder::unlocks);
		builder.save(output, key);
	}

	private void ensureValid() {
		if (type == RecipeType.CUSTOM && customSaver == null) {
			throw new IllegalStateException("Custom recipe must have a custom saver");
		}
		if (type != RecipeType.CUSTOM && result == null && type != RecipeType.SMITHING_TRIM) {
			throw new IllegalStateException("Result must be set for non-custom, non-trim recipes");
		}
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
		SMITHING_TRIM,
		CUSTOM
	}

	@FunctionalInterface
	private interface RecipeSaver {
		void save(RecipeOutput output, ResourceKey<Recipe<?>> id);
	}
}