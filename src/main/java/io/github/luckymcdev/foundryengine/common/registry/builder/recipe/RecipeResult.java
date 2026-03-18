package io.github.luckymcdev.foundryengine.common.registry.builder.recipe;

import io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.AbstractJRecipe;
import net.minecraft.resources.Identifier;

public record RecipeResult(Identifier id, AbstractJRecipe get) {
}
