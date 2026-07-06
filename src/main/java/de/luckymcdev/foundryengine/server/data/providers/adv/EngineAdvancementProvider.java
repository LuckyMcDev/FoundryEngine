package de.luckymcdev.foundryengine.server.data.providers.adv;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EngineAdvancementProvider extends AdvancementProvider {

	public EngineAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, List<AdvancementSubProvider> subProviders) {
		super(output, registries, subProviders);
	}
}
