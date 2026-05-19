package de.luckymcdev.foundryengine.common.data.providers.client;

import de.luckymcdev.foundryengine.api.event.registry.RegistryEvent;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilderImpl;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class EngineSoundDefinitionsProvider extends SoundDefinitionsProvider {
    private final String bundleId;

    public EngineSoundDefinitionsProvider(PackOutput output, String modId) {
        super(output, modId);
        this.bundleId = modId;
    }

    @Override
    public void registerSounds() {
        Bundle bundle = Common.getBundleManager().getBundle(bundleId);
        BundleRegistryQuery query = bundle.registryQuery();

        for (SoundEvent sound : query.getSoundEvents()) {
            SoundBuilderImpl builder = RegistryEvent.getSoundBuilder(sound.location());
            if (builder == null) continue;

            SoundDefinition def = definition();
            if (builder.getSubtitle() != null) {
                def.subtitle(builder.getSubtitle());
            }
            def.replace(builder.isReplace());

            if (builder.getSoundFiles().isEmpty()) {
                def.with(sound(sound.location()));
            } else {
                for (var entry : builder.getSoundFiles()) {
                    SoundDefinition.Sound soundEntry = sound(entry.location())
                            .volume(entry.volume())
                            .pitch(entry.pitch())
                            .weight(entry.weight());
                    if (entry.stream()) soundEntry.stream();
                    soundEntry.attenuationDistance(entry.attenuationDistance());
                    if (entry.preload()) soundEntry.preload();
                    def.with(soundEntry);
                }
            }

            add(sound, def);
        }
    }
}
