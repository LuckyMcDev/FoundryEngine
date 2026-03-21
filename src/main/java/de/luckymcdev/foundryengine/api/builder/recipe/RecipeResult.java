package de.luckymcdev.foundryengine.api.builder.recipe;

import de.luckymcdev.foundryengine.common.vpacks.json.recipe.AbstractJRecipe;
import net.minecraft.resources.Identifier;

public record RecipeResult(Identifier id, AbstractJRecipe get) {
}
