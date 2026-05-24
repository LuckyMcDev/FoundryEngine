package de.luckymcdev.foundryengine.common.data.providers.server.recipe;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.RecipePrioritiesProvider;

import java.util.concurrent.CompletableFuture;

public class EngineRecipePrioritiesProvider extends RecipePrioritiesProvider implements EngineProviderExtension {
    private final Bundle bundle;

    public EngineRecipePrioritiesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Bundle bundle) {
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
