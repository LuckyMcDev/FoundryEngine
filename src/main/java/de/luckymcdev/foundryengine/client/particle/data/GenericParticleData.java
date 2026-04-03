package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.client.particle.AbstractEngineParticle;

public interface GenericParticleData {
    void apply(AbstractEngineParticle particle, int age, int lifetime);
}
