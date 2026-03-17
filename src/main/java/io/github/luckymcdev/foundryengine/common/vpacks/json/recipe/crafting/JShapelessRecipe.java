package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.crafting;

import io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.JIngredient;

import java.util.ArrayList;
import java.util.List;

public class JShapelessRecipe extends AbstractJCraftingRecipe<JShapelessRecipe> {
    private final List<JIngredient> ingredients = new ArrayList<>();

    public JShapelessRecipe() {
        super("minecraft:crafting_shapeless");
    }

    public JShapelessRecipe ingredient(JIngredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public JShapelessRecipe ingredients(JIngredient... ingredients) {
        this.ingredients.addAll(List.of(ingredients));
        return this;
    }
}
