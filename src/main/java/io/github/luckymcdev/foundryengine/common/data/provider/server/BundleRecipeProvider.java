package io.github.luckymcdev.foundryengine.common.data.provider.server;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class BundleRecipeProvider extends RecipeProvider {
    public BundleRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // This is empty, as I haven't gotten around to implementing this yet, but is added in anticipation to it.
    }

    public static class Runner extends RecipeProvider.Runner {
        private final Bundle bundle;

        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, Bundle bundle) {
            super(output, lookupProvider);
            this.bundle = bundle;
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new BundleRecipeProvider(provider, output);
        }

        @Override
        public @NonNull String getName() {
            return bundle.info().id();
        }
    }

}
