package de.luckymcdev.foundryengine.server.data.providers.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.RecipePrioritiesProvider;

import java.util.concurrent.CompletableFuture;

public class EngineRecipePrioritiesProvider extends RecipePrioritiesProvider {
    private final String namespace;

    public EngineRecipePrioritiesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String namespace) {
        super(output, registries, namespace);
        this.namespace = namespace;
    }

    @Override
    protected void start() {
    }
}
