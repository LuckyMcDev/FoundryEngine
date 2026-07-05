package de.luckymcdev.foundryengine.server.data.providers.recipe;

import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EngineRecipeProvider extends RecipeProvider {
    private final List<RecipeBuilder> recipeBuilders;

    public EngineRecipeProvider(HolderLookup.Provider registries, RecipeOutput output, List<RecipeBuilder> recipeBuilders) {
        super(registries, output);
        this.recipeBuilders = recipeBuilders;
    }

    @Override
    protected void buildRecipes() {
        for (RecipeBuilder builder : recipeBuilders) {
            var recipe = builder.build();
            recipe.accept(output, registries);
        }
    }

    public static class Runner extends RecipeProvider.Runner {
        private final String namespace;
        private final List<RecipeBuilder> recipeBuilders;

        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, String namespace, List<RecipeBuilder> recipeBuilders) {
            super(packOutput, registries);
            this.namespace = namespace;
            this.recipeBuilders = recipeBuilders;
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new EngineRecipeProvider(registries, output, recipeBuilders);
        }

        @Override
        public String getName() {
            return "EngineRecipeProvider: " + namespace;
        }
    }
}
