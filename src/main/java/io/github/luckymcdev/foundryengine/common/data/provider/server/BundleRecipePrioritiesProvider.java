package io.github.luckymcdev.foundryengine.common.data.provider.server;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.RecipePrioritiesProvider;

import java.util.concurrent.CompletableFuture;

public class BundleRecipePrioritiesProvider extends RecipePrioritiesProvider {
    public BundleRecipePrioritiesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Bundle bundle) {
        super(output, registries, bundle.info().id());
    }

    @Override
    protected void start() {
        // This is empty, as I haven't gotten around to implementing this yet, but is added in anticipation to it.
    }
}
