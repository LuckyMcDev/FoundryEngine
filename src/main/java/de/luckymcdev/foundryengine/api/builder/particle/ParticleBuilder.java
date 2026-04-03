package de.luckymcdev.foundryengine.api.builder.particle;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.client.particle.EngineParticleSpec;
import de.luckymcdev.foundryengine.client.particle.data.GenericParticleData;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilderImpl;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Function;

/**
 * Builder interface for creating and customizing Particles.
 * Provides a fluent API for particle registration with NeoForge.
 */
public interface ParticleBuilder extends BuilderBase<ParticleType<?>> {

    /**
     * Creates a new ParticleBuilder instance.
     *
     * @param id The identifier for this particle type
     * @return A new ParticleBuilder
     */
    static ParticleBuilder create(Identifier id) {
        return new ParticleBuilderImpl(id);
    }

    /**
     * Sets a custom factory for creating the particle type.
     *
     * @param factory Function that takes the alwaysShow flag and returns a ParticleType
     * @return This builder for chaining
     */
    ParticleBuilder factory(Function<Boolean, ParticleType<?>> factory);

    /**
     * Marks this particle type to always render, even when particle limit is reached.
     *
     * @return This builder for chaining
     */
    ParticleBuilder alwaysShow();

    /**
     * Sets whether this particle type should always render.
     *
     * @param alwaysShow Whether the particle always renders
     * @return This builder for chaining
     */
    ParticleBuilder alwaysShow(boolean alwaysShow);

    /**
     * Sets the full particle spec directly.
     *
     * @param spec The spec to use
     * @return This builder for chaining
     */
    ParticleBuilder spec(EngineParticleSpec spec);

    /**
     * Sets the particle lifetime in ticks.
     *
     * @param lifetime The particle lifetime
     * @return This builder for chaining
     */
    ParticleBuilder lifetime(int lifetime);

    /**
     * Sets the particle position.
     *
     * @param position The particle position
     * @return This builder for chaining
     */
    ParticleBuilder position(Vector3f position);

    /**
     * Sets the particle position.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param z The Z coordinate
     * @return This builder for chaining
     */
    ParticleBuilder position(float x, float y, float z);

    /**
     * Sets the particle velocity.
     *
     * @param position The particle velocity
     * @return This builder for chaining
     */
    ParticleBuilder velocity(Vector3f position);

    /**
     * Sets the particle velocity.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param z The Z coordinate
     * @return This builder for chaining
     */
    ParticleBuilder velocity(float x, float y, float z);

    /**
     * Sets the particle render layer.
     *
     * @param layer The particle layer
     * @return This builder for chaining
     */
    ParticleBuilder layer(SingleQuadParticle.Layer layer);

    /**
     * Adds a single data modifier for this particle.
     *
     * @param data The data modifier
     * @return This builder for chaining
     */
    ParticleBuilder addData(GenericParticleData data);

    /**
     * Adds multiple data modifiers for this particle.
     *
     * @param data The data modifiers
     * @return This builder for chaining
     */
    ParticleBuilder data(GenericParticleData... data);

    /**
     * Adds multiple data modifiers for this particle.
     *
     * @param data The data modifiers
     * @return This builder for chaining
     */
    ParticleBuilder data(List<GenericParticleData> data);

    /**
     * Registers this particle type using the provided helper.
     *
     * @param helper The register event helper
     * @return The registered ParticleType instance
     */
    @ApiStatus.Internal
    ParticleType<?> register(RegisterEvent.RegisterHelper<ParticleType<?>> helper);
}
