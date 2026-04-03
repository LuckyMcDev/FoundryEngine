package de.luckymcdev.foundryengine.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public class EngineParticle extends AbstractEngineParticle {
    public EngineParticle(Identifier id, ClientLevel level, double x, double y, double z, SpriteSet sprites, EngineParticleSpec spec) {
        super(id, level, x, y, z, sprites, spec);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final Identifier id;
        private final EngineParticleSpec spec;
        private final SpriteSet sprites;

        public Provider(Identifier id, EngineParticleSpec spec, SpriteSet sprites) {
            this.id = id;
            this.spec = spec;
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource randomSource) {
            EngineParticle particle = new EngineParticle(this.id, clientLevel, x, y, z, this.sprites, this.spec);
            particle.setParticleSpeed(xSpeed, ySpeed, zSpeed);
            return particle;
        }
    }
}
