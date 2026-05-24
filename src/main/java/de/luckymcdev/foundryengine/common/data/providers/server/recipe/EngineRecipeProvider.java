package de.luckymcdev.foundryengine.common.data.providers.server.recipe;

import de.luckymcdev.foundryengine.api.builder.recipe.RecipeResult;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public class EngineRecipeProvider extends RecipeProvider implements EngineProviderExtension {
    private final Bundle bundle;

    public EngineRecipeProvider(HolderLookup.Provider registries, RecipeOutput output, Bundle bundle) {
        super(registries, output);
        this.bundle = bundle;
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }

    @Override
    protected void buildRecipes() {
        for (RecipeResult recipe : bundle.registryQuery().getRecipes()) {
            recipe.save().accept(output, registries);
        }
    }

    public static class Runner extends RecipeProvider.Runner {
        private final Bundle bundle;

        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries, Bundle bundle) {
            super(packOutput, registries);
            this.bundle = bundle;
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new EngineRecipeProvider(registries, output, bundle);
        }

        @Override
        public String getName() {
            return "EngineRecipeProvider: " + bundle.info().id();
        }
    }
}
