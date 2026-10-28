package de.luckymcdev.foundryengine.data.server.providers.adv;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;

import java.util.function.Consumer;

public record EngineAdvancementSubProvider() implements AdvancementSubProvider {

	@Override
	public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
	}
}
