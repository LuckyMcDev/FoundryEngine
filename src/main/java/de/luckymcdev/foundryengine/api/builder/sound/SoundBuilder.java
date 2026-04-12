package de.luckymcdev.foundryengine.api.builder.sound;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilderImpl;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Builder interface for creating and registering SoundEvents.
 * Provides a fluent API for sound registration with NeoForge.
 */
public interface SoundBuilder extends BuilderBase<SoundEvent> {

    /**
     * Creates a new SoundBuilder instance.
     *
     * @param id The identifier for this sound event
     * @return A new SoundBuilder
     */
    static SoundBuilder create(Identifier id) {
        return new SoundBuilderImpl(id);
    }

    /**
     * Sets a fixed attenuation distance for this sound.
     * Defaults to variable range if not set.
     *
     * @param distance The fixed range in blocks
     * @return This builder for chaining
     */
    SoundBuilder range(float distance);

    /**
     * Registers this sound event using the provided helper.
     *
     * @param helper The register event helper
     * @return The registered SoundEvent instance
     */
    @ApiStatus.Internal
    SoundEvent register(RegisterEvent.RegisterHelper<SoundEvent> helper);
}