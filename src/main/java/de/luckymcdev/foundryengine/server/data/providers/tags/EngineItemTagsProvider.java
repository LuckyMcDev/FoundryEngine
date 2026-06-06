package de.luckymcdev.foundryengine.server.data.providers.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class EngineItemTagsProvider extends ItemTagsProvider {
    private final String namespace;

    public EngineItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String namespace) {
        super(output, lookupProvider, namespace);
        this.namespace = namespace;
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
    }
}
