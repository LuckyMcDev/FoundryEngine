package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.smelting;

import com.google.gson.annotations.SerializedName;
import io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.AbstractJResultingRecipe;
import io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.JIngredient;

public class JSmeltingRecipe extends AbstractJResultingRecipe<JSmeltingRecipe> {
    private String category;
    private String group;
    private JIngredient ingredient;
    private int experience;
    @SerializedName("cookingtime")
    private int cookingTime;

    /**
     * Creates a new JSmeltingRecipe object.
     *
     * @param type The associated recipe type with the object. {@link SmeltingTypes}
     *             contains the vanilla values.
     * @see SmeltingTypes
     */
    public JSmeltingRecipe(String type) {
        super(type);
    }

    public JSmeltingRecipe category(String category) {
        this.category = category;
        return this;
    }

    public JSmeltingRecipe group(String group) {
        this.group = group;
        return this;
    }

    public JSmeltingRecipe experience(int experience) {
        this.experience = experience;
        return this;
    }

    public JSmeltingRecipe cookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
        return this;
    }

    public JSmeltingRecipe ingredient(JIngredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }
}
