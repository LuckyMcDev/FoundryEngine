package io.github.luckymcdev.foundryengine.common.data.provider.server;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public class BundleDataMapProvider extends DataMapProvider {

    public BundleDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        // This is empty, as there is currently no easy way to generate this for each bundle / i havent gotten around to it.
    }
}
