package de.luckymcdev.foundryengine.api.event.data;

import de.luckymcdev.foundryengine.common.data.EngineDataGenerator;
import net.minecraft.data.DataProvider;

public record BundleDataGenEvent(EngineDataGenerator generator, String namespace) {

    public void addProvider(DataProvider provider) {
        generator.addProvider(provider);
    }
}
