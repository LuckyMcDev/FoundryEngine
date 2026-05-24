package de.luckymcdev.foundryengine.common.data.providers.server.loot;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;

public class EngineLootTableSubProvider implements LootTableSubProvider, EngineProviderExtension {
    private final Bundle bundle;

    public EngineLootTableSubProvider(Bundle bundle, HolderLookup.Provider registries) {
        this.bundle = bundle;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
