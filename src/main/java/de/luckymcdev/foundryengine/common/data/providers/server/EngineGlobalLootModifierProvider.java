package de.luckymcdev.foundryengine.common.data.providers.server;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class EngineGlobalLootModifierProvider extends GlobalLootModifierProvider implements EngineProviderExtension {
    private final Bundle bundle;

    public EngineGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Bundle bundle) {
        super(output, registries, bundle.info().id());
        this.bundle = bundle;
    }

    @Override
    protected void start() {
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
