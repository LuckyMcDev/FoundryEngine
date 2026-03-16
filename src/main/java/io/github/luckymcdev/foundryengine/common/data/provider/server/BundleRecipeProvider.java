package io.github.luckymcdev.foundryengine.common.data.provider.server;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class BundleRecipeProvider extends RecipeProvider {
    private final Bundle bundle;

    public BundleRecipeProvider(HolderLookup.Provider registries, RecipeOutput output, Bundle bundle) {
        super(registries, output);
        this.bundle = bundle;
    }

// Inside BundleRecipeProvider.java

    @Override
    protected void buildRecipes() {
        var itemLookup = registries.lookupOrThrow(Registries.ITEM);

        bundle.registryQuery().getRecipeBuilders().forEach(recipeBuilder ->
                recipeBuilder.withItemGetter(itemLookup).save(output)
        );
    }

    public static class Runner extends RecipeProvider.Runner {
        private final Bundle bundle;

        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, Bundle bundle) {
            super(output, lookupProvider);
            this.bundle = bundle;
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider provider, @NonNull RecipeOutput output) {
            return new BundleRecipeProvider(provider, output, bundle);
        }

        @Override
        public @NonNull String getName() {
            return bundle.info().id();
        }
    }

}
