package de.luckymcdev.foundryengine.common.data.providers.server.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.RecipePrioritiesProvider;

import java.util.concurrent.CompletableFuture;

public class EngineRecipePrioritiesProvider extends RecipePrioritiesProvider {
    public EngineRecipePrioritiesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        super(output, registries, modid);
    }

    @Override
    protected void start() {

    }
}
