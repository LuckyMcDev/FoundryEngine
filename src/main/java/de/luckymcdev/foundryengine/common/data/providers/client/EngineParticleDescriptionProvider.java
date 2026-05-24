package de.luckymcdev.foundryengine.common.data.providers.client;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.data.providers.EngineProviderExtension;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

public class EngineParticleDescriptionProvider extends ParticleDescriptionProvider implements EngineProviderExtension {
    private final Bundle bundle;

    public EngineParticleDescriptionProvider(PackOutput output, Bundle bundle) {
        super(output);
        this.bundle = bundle;
    }

    @Override
    protected void addDescriptions() {
    }

    @Override
    public Bundle bundle() {
        return bundle;
    }
}
