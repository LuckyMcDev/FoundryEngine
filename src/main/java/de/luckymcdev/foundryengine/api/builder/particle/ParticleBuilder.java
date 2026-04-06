package de.luckymcdev.foundryengine.api.builder.particle;

import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.client.particle.data.*;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;

import java.util.function.Function;

/**
 * A fluent builder for creating and configuring particle types and their behavior.
 * This builder uses a single-data model where only one sequence per data type
 * (Color, Scale, etc.) is permitted.
 */
public interface ParticleBuilder extends BuilderBase<ParticleType<?>> {

    /**
     * Creates a new ParticleBuilder instance.
     *
     * @param id The identifier for this particle type
     * @return A new ParticleBuilder
     */
    static ParticleBuilder create(Identifier id) {
        return new de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilderImpl(id);
    }

    /**
     * Sets a custom factory for creating the particle type.
     * Useful for creating custom particle types beyond the standard SimpleParticleType.
     */
    ParticleBuilder factory(Function<Boolean, ParticleType<?>> factory);

    /**
     * Marks this particle type to always render, even when the particle limit is reached
     * or at a distance.
     */
    ParticleBuilder alwaysShow();

    /**
     * Sets the default lifetime of particles created with this builder.
     *
     * @param lifetime Lifetime in ticks
     */
    ParticleBuilder lifetime(int lifetime);

    /**
     * Sets the rendering layer for the particle (e.g., OPAQUE or TRANSLUCENT).
     */
    ParticleBuilder layer(ParticleLayer layer);

    /**
     * Sets the color behavior using a complete data object.
     */
    ParticleBuilder colorData(ParticleColorData data);

    /**
     * Sets a constant color for the particle's entire lifetime.
     */
    default ParticleBuilder color(Color color) {
        return colorData(new ParticleColorData(new KeyframeSequence<Color>().add(color, 0, Easing.LINEAR)));
    }

    /**
     * Sets a color interpolation from a start color to an end color.
     */
    default ParticleBuilder color(Color start, Color end, Easing easing) {
        return colorData(new ParticleColorData(new KeyframeSequence<Color>()
                .add(start, 0, Easing.LINEAR)
                .add(end, 1, easing)));
    }

    /**
     * Sets the scale behavior using a complete data object.
     */
    ParticleBuilder scaleData(ParticleScaleData data);

    /**
     * Sets a constant scale for the particle's entire lifetime.
     */
    default ParticleBuilder scale(float scale) {
        return scaleData(new ParticleScaleData(new KeyframeSequence<Float>().add(scale, 0, Easing.LINEAR)));
    }

    /**
     * Sets the velocity behavior using a complete data object.
     */
    ParticleBuilder velocityData(ParticleVelocityData data);

    /**
     * Sets a constant velocity/speed for the particle's entire lifetime.
     */
    default ParticleBuilder velocity(Vector3d vel) {
        return velocityData(new ParticleVelocityData(new KeyframeSequence<Vector3d>().add(vel, 0, Easing.LINEAR)));
    }

    /**
     * Sets the position behavior using a complete data object.
     */
    ParticleBuilder positionData(ParticlePositionData data);

    /**
     * Sets a constant position offset for the particle's entire lifetime.
     */
    default ParticleBuilder position(Vector3d pos) {
        return positionData(new ParticlePositionData(new KeyframeSequence<Vector3d>().add(pos, 0, Easing.LINEAR)));
    }

    /**
     * Sets the rotation behavior using a complete data object.
     */
    ParticleBuilder rotationData(ParticleRotationData data);

    /**
     * Sets a constant rotation (in radians) for the particle's entire lifetime.
     */
    default ParticleBuilder rotation(float radians) {
        return rotationData(new ParticleRotationData(new KeyframeSequence<Float>().add(radians, 0, Easing.LINEAR)));
    }

    /**
     * Sets a rotation animation from start to end (in radians).
     */
    default ParticleBuilder rotation(float start, float end, Easing easing) {
        return rotationData(new ParticleRotationData(new KeyframeSequence<Float>()
                .add(start, 0, Easing.LINEAR)
                .add(end, 1, easing)));
    }

    @ApiStatus.Internal
    ParticleType<?> register(RegisterEvent.RegisterHelper<ParticleType<?>> helper);
}