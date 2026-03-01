package io.github.luckymcdev.foundryengine.common.data;

import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.bundle.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EngineGenerator {
    private final List<BundleGenerator> generators = new ArrayList<>();

    public EngineGenerator() {
        for (Bundle bundle : Common.getBundleManager().getBundles()) {
            generators.add(new BundleGenerator(bundle));
        }
    }

    public void run() throws IOException {
        for (BundleGenerator generator : generators) {
            generator.run();
        }
    }
}
