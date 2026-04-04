package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.client.particle.EngineParticle;

public interface GenericParticleData {
    void apply(EngineParticle particle, int age, int lifetime);
}
