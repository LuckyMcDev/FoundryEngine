package de.luckymcdev.foundryengine.common.vpacks.json.recipe.crafting;

import de.luckymcdev.foundryengine.common.vpacks.json.recipe.JIngredient;

import java.util.HashMap;
import java.util.Map;

public class JShapedRecipe extends AbstractJCraftingRecipe<JShapedRecipe> {
    private final String[] pattern = new String[3];
    private final Map<String, JIngredient> key = new HashMap<>();

    public JShapedRecipe() {
        super("minecraft:crafting_shaped");
    }

    public JShapedRecipe row(int index, String row) {
        if (index > 2) {
            throw new IllegalStateException("There are only three rows in the crafting grid, therefore the highest index is 2.");
        }
        this.pattern[index] = row;
        return this;
    }

    public JShapedRecipe key(String key, JIngredient ingredient) {
        this.key.put(key, ingredient);
        return this;
    }
}
