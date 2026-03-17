package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.smithing;

import io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.JIngredient;

public class JSmithingTrimRecipe extends AbstractJSmithingRecipe<JSmithingTrimRecipe> {
    public JSmithingTrimRecipe() {
        super("minecraft:smithing_transform");
    }

    /**
     * Adds the "Trimmable Armor" (#minecraft:trimmable_armor) tag as the base.
     *
     * @return The current instance of {@link AbstractJSmithingRecipe}
     */
    public JSmithingTrimRecipe trimmableArmor() {
        return this.base(new JIngredient().tag("minecraft:trimmable_armor"));
    }
}
