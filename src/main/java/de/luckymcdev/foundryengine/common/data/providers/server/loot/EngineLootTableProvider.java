package de.luckymcdev.foundryengine.common.data.providers.server.loot;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class EngineLootTableProvider extends LootTableProvider implements EngineProviderExtension {
    private final Bundle bundle;

    public EngineLootTableProvider(PackOutput output, Set<ResourceKey<LootTable>> requiredTables, List<SubProviderEntry> subProviders, CompletableFuture<HolderLookup.Provider> registries, Bundle bundle) {
        super(output, requiredTables, subProviders, registries);
        this.bundle = bundle;
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
