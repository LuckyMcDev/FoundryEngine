package de.luckymcdev.foundryengine.api.event.data;

import de.luckymcdev.foundryengine.common.data.EngineDataGenerator;
import net.minecraft.data.DataProvider;
import net.neoforged.bus.api.Event;

public class BundleDataGenEvent extends Event {
    private final EngineDataGenerator generator;

    public BundleDataGenEvent(EngineDataGenerator generator) {
        this.generator = generator;
    }

    public EngineDataGenerator getGenerator() {
        return generator;
    }

    public void addProvider(DataProvider provider) {
        generator.addProvider(provider);
    }
}
