package io.github.luckymcdev.foundryengine.common.data.provider.server;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BundleDatapackBuiltinEntriesProvider extends DatapackBuiltinEntriesProvider {
    public BundleDatapackBuiltinEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, RegistrySetBuilder datapackEntriesBuilder, Set<String> modIds) {
        super(output, registries, datapackEntriesBuilder, modIds);
    }
}
