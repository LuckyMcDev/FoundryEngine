package io.github.luckymcdev.foundryengine.common.generator;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.SharedConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

import java.io.IOException;

public class BundleGenerator {
    private final DataGenerator dataGenerator;

    public BundleGenerator(Bundle bundle) {
        this.dataGenerator = new DataGenerator(
                bundle.bundleFiles().root(),
                SharedConstants.getCurrentVersion(),
                false
        );
    }

    public void run() throws IOException {
        dataGenerator.run();
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return dataGenerator.addProvider(true, provider);
    }

}
