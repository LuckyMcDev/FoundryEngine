package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.smithing;

import io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.AbstractJRecipe;
import io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.JIngredient;

public abstract class AbstractJSmithingRecipe<T extends AbstractJSmithingRecipe<T>> extends AbstractJRecipe {
    private JIngredient base;
    private JIngredient addition;
    private JIngredient template;

    public AbstractJSmithingRecipe(String type) {
        super(type);
    }

    public T base(JIngredient base) {
        this.base = base;
        return (T) this;
    }

    public T addition(JIngredient addition) {
        this.addition = addition;
        return (T) this;
    }

    public T template(JIngredient template) {
        this.template = template;
        return (T) this;
    }
}
