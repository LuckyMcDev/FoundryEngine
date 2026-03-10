package io.github.luckymcdev.foundryengine.common.data;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import io.github.luckymcdev.foundryengine.common.data.provider.server.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Handles server-side data generation for bundles.
 * Server data includes: recipes, loot tables, advancements, tags, worldgen, data-maps.
 */
public class BundleServerGenerator {
    private final Bundle bundle;
    private final DataGenerator dataGenerator;
    private final PackOutput pOut;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public BundleServerGenerator(Bundle bundle, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.bundle = bundle;
        this.lookupProvider = lookupProvider;
        this.dataGenerator = new DataGenerator(
                this.bundle.bundleFiles().generated(),
                SharedConstants.getCurrentVersion(),
                true
        );
        this.pOut = dataGenerator.getPackOutput();
        addServerProviders();
    }

    private void addServerProviders() {
        addProvider(new BundleAdvancementProvider(pOut, lookupProvider));
        addProvider(new BundleDataMapProvider(pOut, lookupProvider));
        addProvider(new BundleGlobalLootModifierProvider(pOut, lookupProvider, bundle));
        addProvider(new BundleRecipePrioritiesProvider(pOut, lookupProvider, bundle));
        addProvider(new BundleRecipeProvider.Runner(pOut, lookupProvider, bundle));

        Set<ResourceKey<LootTable>> requiredTables = Set.of();
        List<net.minecraft.data.loot.LootTableProvider.SubProviderEntry> subProviders = List.of();
        addProvider(new BundleLootTableProvider(pOut, requiredTables, subProviders, lookupProvider));
    }


    public void createDatapackRegistryObjects(RegistrySetBuilder datapackEntriesBuilder) {
        String bundleId = bundle.info().id();
        addProvider(
                new BundleDatapackBuiltinEntriesProvider(pOut, lookupProvider, datapackEntriesBuilder, Set.of(bundleId))
        );
    }

    public void run() throws IOException {
        dataGenerator.run();
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return dataGenerator.addProvider(true, provider);
    }
}