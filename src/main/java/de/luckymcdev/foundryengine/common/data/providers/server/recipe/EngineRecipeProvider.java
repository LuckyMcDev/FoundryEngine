package de.luckymcdev.foundryengine.common.data.providers.server.recipe;

import de.luckymcdev.foundryengine.api.builder.recipe.RecipeResult;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public class EngineRecipeProvider extends RecipeProvider {
    private final String bundleId;

    public EngineRecipeProvider(HolderLookup.Provider registries, RecipeOutput output, String bundleId) {
        super(registries, output);
        this.bundleId = bundleId;
    }

    @Override
    protected void buildRecipes() {
        Bundle bundle = Common.getBundleManager().getBundle(bundleId);
        if (bundle == null) return;

        for (RecipeResult recipe : bundle.registryQuery().getRecipes()) {
            recipe.save().accept(output, registries);
        }
    }

    public static class Runner extends RecipeProvider.Runner {
        private final String bundleId;

        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, String bundleId) {
            super(packOutput, registries);
            this.bundleId = bundleId;
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new EngineRecipeProvider(registries, output, bundleId);
        }

        @Override
        public String getName() {
            return "EngineRecipeProvider: " + bundleId;
        }
    }
}
