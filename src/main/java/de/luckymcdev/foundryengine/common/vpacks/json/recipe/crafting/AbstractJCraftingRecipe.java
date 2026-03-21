package de.luckymcdev.foundryengine.common.vpacks.json.recipe.crafting;

import de.luckymcdev.foundryengine.common.vpacks.json.recipe.AbstractJRecipe;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.JResult;

public abstract class AbstractJCraftingRecipe<T extends AbstractJCraftingRecipe<T>> extends AbstractJRecipe {
    private String group;
    private JResult result;

    public AbstractJCraftingRecipe(String type) {
        super(type);
    }

    public T group(String group) {
        this.group = group;
        return (T) this;
    }

    public T result(JResult result) {
        this.result = result;
        return (T) this;
    }
}
