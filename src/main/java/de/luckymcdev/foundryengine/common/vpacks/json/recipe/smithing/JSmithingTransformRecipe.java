package de.luckymcdev.foundryengine.common.vpacks.json.recipe.smithing;

import de.luckymcdev.foundryengine.common.vpacks.json.recipe.JResult;

public class JSmithingTransformRecipe extends AbstractJSmithingRecipe<JSmithingTransformRecipe> {
    private JResult result;

    public JSmithingTransformRecipe() {
        super("minecraft:smithing_transform");
    }

    public JSmithingTransformRecipe result(JResult result) {
        this.result = result;
        return this;
    }
}
