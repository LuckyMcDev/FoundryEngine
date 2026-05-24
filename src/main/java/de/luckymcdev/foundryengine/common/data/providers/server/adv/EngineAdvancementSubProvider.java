package de.luckymcdev.foundryengine.common.data.providers.server.adv;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;

import java.util.function.Consumer;

public record EngineAdvancementSubProvider(Bundle bundle) implements AdvancementSubProvider, EngineProviderExtension {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
    }
}
