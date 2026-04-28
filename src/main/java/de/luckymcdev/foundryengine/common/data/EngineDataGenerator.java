package de.luckymcdev.foundryengine.common.data;

import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

import java.io.IOException;
import java.nio.file.Path;

public class EngineDataGenerator {
    private final DataGenerator generator;
    private final Path output;

    public EngineDataGenerator(Path output) {
        this(output, true, false);
    }

    public EngineDataGenerator(Path output, boolean alwaysRun, boolean cached) {
        this.output = output;
        if (cached) {
            generator = new DataGenerator.Cached(output, SharedConstants.getCurrentVersion(), alwaysRun);
        } else {
            generator = new DataGenerator.Uncached(output);
        }
    }

    public void run() throws IOException {
        this.generator.run();
    }

    public void addProvider(DataProvider provider) {
        this.generator.addProvider(true, provider);
    }

    public DataGenerator getGenerator() {
        return generator;
    }

    public Path getOutput() {
        return output;
    }
}
