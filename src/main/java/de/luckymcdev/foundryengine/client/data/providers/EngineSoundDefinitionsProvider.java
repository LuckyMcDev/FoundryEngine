package de.luckymcdev.foundryengine.client.data.providers;

import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilderImpl;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

import java.util.List;

public class EngineSoundDefinitionsProvider extends SoundDefinitionsProvider {
    private final List<SoundBuilderImpl> soundBuilders;

    public EngineSoundDefinitionsProvider(PackOutput output, String namespace, List<SoundBuilderImpl> soundBuilders) {
        super(output, namespace);
        this.soundBuilders = soundBuilders;
    }

    @Override
    public void registerSounds() {
        for (SoundBuilderImpl builder : soundBuilders) {
            SoundEvent sound = builder.get();
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
