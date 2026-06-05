package de.luckymcdev.foundryengine.api.event.data;

import de.luckymcdev.foundryengine.common.data.EngineDataGenerator;
import net.minecraft.data.DataProvider;
import net.neoforged.bus.api.Event;

public class BundleDataGenEvent extends Event {
    private final EngineDataGenerator generator;
    private final String namespace;

    public BundleDataGenEvent(
            EngineDataGenerator generator,
            String namespace
    ) {
        this.generator = generator;
        this.namespace = namespace;
    }

    public EngineDataGenerator getGenerator() {
        return generator;
    }

    public String getNamespace() {
        return namespace;
    }

    public void addProvider(DataProvider provider) {
        generator.addProvider(provider);
    }
}
