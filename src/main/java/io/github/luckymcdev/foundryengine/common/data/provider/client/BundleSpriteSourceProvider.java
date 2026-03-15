package io.github.luckymcdev.foundryengine.common.data.provider.client;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

import java.util.concurrent.CompletableFuture;

public class BundleSpriteSourceProvider extends SpriteSourceProvider {
    public BundleSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, Bundle bundle) {
        super(output, lookupProvider, bundle.info().id());
    }

    @Override
    protected void gather() {
        // Not implemented yet.
    }
}
