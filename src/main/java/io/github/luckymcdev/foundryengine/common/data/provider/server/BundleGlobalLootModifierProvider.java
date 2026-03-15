package io.github.luckymcdev.foundryengine.common.data.provider.server;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class BundleGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public BundleGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Bundle bundle) {
        super(output, registries, bundle.info().id());
    }

    @Override
    protected void start() {
        // This method is empty, as there is no good way to generate a global loot modifier for a bundle ye.
    }
}
