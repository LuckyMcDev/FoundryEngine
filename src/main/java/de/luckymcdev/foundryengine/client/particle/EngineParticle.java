package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.client.particle.data.GenericParticleData;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public class EngineParticle extends SingleQuadParticle {
    private final Identifier id;
    private final SpriteSet sprites;
    private final EngineParticleSpec spec;
    private final float baseQuadSize;

    public EngineParticle(Identifier id, ClientLevel level, double x, double y, double z, SpriteSet sprites, EngineParticleSpec spec) {
        super(level, x, y, z, sprites.first());
        this.id = id;
        this.sprites = sprites;
        this.spec = spec;
        this.baseQuadSize = this.quadSize;
        this.lifetime = spec.lifetime();
        applyData(0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        setSpriteFromAge(sprites);
        applyData(this.age);
    }

    @Override
    protected Layer getLayer() {
        return spec.layer();
    }

    public void applyColor(Color color) {
        setColor(color.r(), color.g(), color.b());
        setAlpha(color.a());
    }

    public void applyScale(float scale) {
        this.quadSize = this.baseQuadSize * scale;
        this.setSize(0.2F * scale, 0.2F * scale);
    }

    private void applyData(int age) {
        for (GenericParticleData data : spec.data()) {
            data.apply(this, age, this.lifetime);
        }
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
