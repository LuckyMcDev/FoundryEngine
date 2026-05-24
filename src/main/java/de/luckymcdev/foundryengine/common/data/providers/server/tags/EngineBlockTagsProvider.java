package de.luckymcdev.foundryengine.common.data.providers.server.tags;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class EngineBlockTagsProvider extends BlockTagsProvider implements EngineProviderExtension {
    private final Bundle bundle;

    public EngineBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, Bundle bundle) {
        super(output, lookupProvider, bundle.info().id());
        this.bundle = bundle;
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
