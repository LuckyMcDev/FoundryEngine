package de.luckymcdev.foundryengine.api.builder.particle;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.client.particle.EngineParticleSpec;
import de.luckymcdev.foundryengine.client.particle.data.*;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilderImpl;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;

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
     */
    ParticleBuilder factory(Function<Boolean, ParticleType<?>> factory);

    /**
     * Marks this particle type to always render, even when the particle limit is reached.
     */
    ParticleBuilder alwaysShow();

    /**
     * Sets whether this particle type should always render.
     */
    ParticleBuilder alwaysShow(boolean alwaysShow);

    /**
     * Sets the full particle spec directly, replacing any existing lifetime, layer and data.
     */
    ParticleBuilder spec(EngineParticleSpec spec);

    /**
     * Sets the particle lifetime in ticks.
     */
    ParticleBuilder lifetime(int lifetime);

    /**
     * Sets the particle render layer.
     */
    ParticleBuilder layer(SingleQuadParticle.Layer layer);

    /**
     * Appends a single generic data modifier.
     */
    ParticleBuilder addData(GenericParticleData data);

    /**
     * Appends multiple generic data modifiers.
     */
    ParticleBuilder data(GenericParticleData... data);

    /**
     * Appends a list of generic data modifiers.
     */
    ParticleBuilder data(List<GenericParticleData> data);

    /**
     * Appends a {@link ParticleColorData} entry.
     */
    ParticleBuilder addColorData(ParticleColorData colorData);

    /**
     * Convenience: interpolates from {@code start} to {@code end} using {@link Easing#LINEAR}.
     */
    default ParticleBuilder color(Color start, Color end) {
        return addColorData(new ParticleColorData(start, end));
    }

    /**
     * Convenience: interpolates from {@code start} to {@code end} using the given easing.
     */
    default ParticleBuilder color(Color start, Color end, Easing easing) {
        return addColorData(new ParticleColorData(start, end, easing));
    }

    /**
     * Appends a {@link ParticleScaleData} entry.
     */
    ParticleBuilder addScaleData(ParticleScaleData scaleData);

    /**
     * Convenience: interpolates from {@code start} to {@code end} using {@link Easing#LINEAR}.
     */
    default ParticleBuilder scale(float start, float end) {
        return addScaleData(new ParticleScaleData(start, end));
    }

    /**
     * Convenience: interpolates from {@code start} to {@code end} using the given easing.
     */
    default ParticleBuilder scale(float start, float end, Easing easing) {
        return addScaleData(new ParticleScaleData(start, end, easing));
    }

    /**
     * Appends a {@link ParticleVelocityData} entry.
     */
    ParticleBuilder addVelocityData(ParticleVelocityData velocityData);

    /**
     * Convenience: constant velocity over the particle's lifetime.
     */
    default ParticleBuilder velocity(Vector3d velocity) {
        return addVelocityData(new ParticleVelocityData(velocity));
    }

    /**
     * Convenience: interpolates velocity from {@code start} to {@code end} using {@link Easing#LINEAR}.
     */
    default ParticleBuilder velocity(Vector3d start, Vector3d end) {
        return addVelocityData(new ParticleVelocityData(start, end));
    }

    /**
     * Convenience: interpolates velocity from {@code start} to {@code end} using the given easing.
     */
    default ParticleBuilder velocity(Vector3d start, Vector3d end, Easing easing) {
        return addVelocityData(new ParticleVelocityData(start, end, easing));
    }

    /**
     * Appends a {@link ParticlePositionData} entry.
     */
    ParticleBuilder addPositionData(ParticlePositionData positionData);

    /**
     * Convenience: interpolates from {@code start} to {@code end} using {@link Easing#LINEAR}.
     */
    default ParticleBuilder positionData(Vector3d start, Vector3d end) {
        return addPositionData(new ParticlePositionData(start, end));
    }

    /**
     * Convenience: interpolates from {@code start} to {@code end} using the given easing.
     */
    default ParticleBuilder positionData(Vector3d start, Vector3d end, Easing easing) {
        return addPositionData(new ParticlePositionData(start, end, easing));
    }

    @ApiStatus.Internal
    ParticleType<?> register(RegisterEvent.RegisterHelper<ParticleType<?>> helper);
}