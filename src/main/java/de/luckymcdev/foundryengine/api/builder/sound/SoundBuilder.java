package de.luckymcdev.foundryengine.api.builder.sound;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilderImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Builder interface for creating and registering SoundEvents.
 */
public interface SoundBuilder extends BuilderBase<SoundEvent> {

    static SoundBuilder create(Identifier id) {
        return new SoundBuilderImpl(id);
    }

    /**
     * Sets the default attenuation distance for the SoundEvent in-game.
     */
    SoundBuilder range(float distance);

    /**
     * Sets the translation key for the sound's subtitle (Closed Captions).
     */
    SoundBuilder subtitle(String translationKey);

    /**
     * If true, this definition replaces the vanilla definition instead of merging.
     */
    SoundBuilder replace(boolean replace);

    /**
     * Adds a sound file to this event.
     * * @param location The path to the .ogg (without /sounds/)
     *
     * @param volume      The volume (0.0 - 1.0+)
     * @param pitch       The pitch (0.0 - 2.0)
     * @param weight      The probability weight (integer)
     * @param stream      True if the file should be streamed from disk (for long tracks)
     * @param attenuation The block distance for volume reduction
     * @param preload     True to load during resource pack loading instead of first play
     */
    SoundBuilder addSound(Identifier location, float volume, float pitch, int weight, boolean stream, int attenuation, boolean preload);

    /**
     * Adds a sound file using default vanilla values.
     */
    default SoundBuilder addSound(Identifier location) {
        return addSound(location, 1.0f, 1.0f, 1, false, 16, false);
    }

    @ApiStatus.Internal
    SoundEvent register(RegisterEvent.RegisterHelper<SoundEvent> helper);
}