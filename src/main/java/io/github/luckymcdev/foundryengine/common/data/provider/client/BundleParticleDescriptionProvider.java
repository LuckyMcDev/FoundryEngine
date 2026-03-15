package io.github.luckymcdev.foundryengine.common.data.provider.client;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

public class BundleParticleDescriptionProvider extends ParticleDescriptionProvider {

    public BundleParticleDescriptionProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        // This is empty, as I haven't gotten around to implementing this yet, but is added in anticipation to it.
    }
}
