package de.luckymcdev.foundryengine.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class EngineParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprites;

    public EngineParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                   double x, double y, double z,
                                   double xd, double yd, double zd, RandomSource random) {
        return new EngineParticle(level, x, y, z, sprites);
    }
}
