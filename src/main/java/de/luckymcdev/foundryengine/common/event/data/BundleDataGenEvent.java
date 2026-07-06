package de.luckymcdev.foundryengine.common.event.data;

import de.luckymcdev.foundryengine.common.data.EngineDataGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.neoforged.bus.api.Event;

import java.util.concurrent.CompletableFuture;

public class BundleDataGenEvent extends Event {
	private final EngineDataGenerator generator;
	private final CompletableFuture<HolderLookup.Provider> lookup;

	public BundleDataGenEvent(EngineDataGenerator generator, CompletableFuture<HolderLookup.Provider> lookup) {
		this.generator = generator;
		this.lookup = lookup;
	}

	public EngineDataGenerator getGenerator() {
		return generator;
	}

	public CompletableFuture<HolderLookup.Provider> getLookup() {
		return lookup;
	}

	public void addProvider(DataProvider provider) {
		generator.addProvider(provider);
	}
}
