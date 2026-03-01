package io.github.luckymcdev.foundryengine.common.data.provider.client;

import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class BundleSoundDefinitionsProvider extends SoundDefinitionsProvider {

    public BundleSoundDefinitionsProvider(PackOutput output, Bundle bundle) {
        super(output, bundle.info().id());
    }

    @Override
    public void registerSounds() {

    }
}
