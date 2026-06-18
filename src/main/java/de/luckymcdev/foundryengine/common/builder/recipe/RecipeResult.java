package de.luckymcdev.foundryengine.common.builder.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

public record RecipeResult(Identifier id, BiConsumer<RecipeOutput, HolderLookup.Provider> save) {
}
