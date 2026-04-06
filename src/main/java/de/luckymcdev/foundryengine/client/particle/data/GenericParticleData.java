package de.luckymcdev.foundryengine.client.particle.data;

/**
 * A Generic Particle Data class, extended to add function to a Particle via {@link ParticleContext}
 */
public interface GenericParticleData {
    void apply(ParticleContext particle, int age, int lifetime);
}