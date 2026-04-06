package de.luckymcdev.foundryengine.client.particle.data;

/**
 * Server-safe particle data interface.
 *
 * <p>Uses {@link ParticleContext} instead of {@code EngineParticle} so that
 * implementations and this interface itself can be safely loaded on the dedicated server.
 * No {@code net.minecraft.client.*} imports anywhere in this hierarchy.</p>
 */
public interface GenericParticleData {
    void apply(ParticleContext particle, int age, int lifetime);
}