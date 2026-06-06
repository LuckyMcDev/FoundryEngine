package de.luckymcdev.foundryengine.server.data.providers;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class EngineGlobalLootModifierProvider extends GlobalLootModifierProvider {
    private final String namespace;

    public EngineGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String namespace) {
        super(output, registries, namespace);
        this.namespace = namespace;
    }

    @Override
    protected void start() {
    }
}
