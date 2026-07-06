package de.luckymcdev.foundryengine.common.data;

import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Wrapper around Minecraft's DataGenerator for Foundry Engine data generation.
 */
public class EngineDataGenerator {
	private final DataGenerator generator;
	private final Path output;

	/**
	 * Creates a new EngineDataGenerator with the given output path.
	 */
	public EngineDataGenerator(Path output) {
		this(output, true, false);
	}

	/**
	 * Creates a new EngineDataGenerator with custom caching and always-run settings.
	 */
	public EngineDataGenerator(Path output, boolean alwaysRun, boolean cached) {
		this.output = output;
		if (cached) {
			generator = new DataGenerator.Cached(output, SharedConstants.getCurrentVersion(), alwaysRun);
		} else {
			generator = new DataGenerator.Uncached(output);
		}
	}

	/**
	 * Runs all registered data providers.
	 */
	public void run() throws IOException {
		this.generator.run();
	}

	/**
	 * Registers a data provider to be run during generation.
	 */
	public void addProvider(DataProvider provider) {
		this.generator.addProvider(true, provider);
	}

	/**
	 * Returns the underlying Minecraft DataGenerator.
	 */
	public DataGenerator getGenerator() {
		return generator;
	}

	/**
	 * Returns the PackOutput from the underlying generator.
	 */
	public PackOutput getPackOutput() {
		return generator.getPackOutput();
	}

	/**
	 * Returns the output path for generated data.
	 */
	public Path getOutput() {
		return output;
	}
}
