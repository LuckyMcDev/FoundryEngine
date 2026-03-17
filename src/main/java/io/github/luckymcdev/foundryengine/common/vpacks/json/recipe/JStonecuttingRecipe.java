package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe;

public class JStonecuttingRecipe extends AbstractJResultingRecipe<JStonecuttingRecipe> {
    private JIngredient ingredient;

    public JStonecuttingRecipe() {
        super("minecraft:stonecutting");
    }

    public JStonecuttingRecipe ingredient(JIngredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }
}
