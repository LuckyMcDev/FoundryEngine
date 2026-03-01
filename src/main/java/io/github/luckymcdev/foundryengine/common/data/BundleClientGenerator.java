package io.github.luckymcdev.foundryengine.common.data;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import io.github.luckymcdev.foundryengine.common.data.provider.client.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Handles client-side data generation for bundles.
 * Client data includes: models, textures, sounds, particles, equipment assets, languages.
 */
public class BundleClientGenerator {
    private final Bundle bundle;
    private final DataGenerator dataGenerator;
    private final PackOutput pOut;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public BundleClientGenerator(Bundle bundle, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.bundle = bundle;
        this.lookupProvider = lookupProvider;
        this.dataGenerator = new DataGenerator(
                this.bundle.bundleFiles().generated(),
                SharedConstants.getCurrentVersion(),
                true
        );
        this.pOut = dataGenerator.getPackOutput();
        addClientProviders("en_us");
    }

    private void addClientProviders(String locale) {
        addProvider(new BundleLanguageProvider(pOut, bundle, locale));
        addProvider(new BundleModelProvider(pOut, bundle));
        addProvider(new BundleEquipmentAssetProvider(pOut));
        addProvider(new BundleSoundDefinitionsProvider(pOut, bundle));
        addProvider(new BundleParticleDescriptionProvider(pOut));
        addProvider(new BundleSpriteSourceProvider(pOut, lookupProvider, bundle));
    }

    public void run() throws IOException {
        dataGenerator.run();
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return dataGenerator.addProvider(true, provider);
    }
}