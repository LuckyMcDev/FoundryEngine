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

import java.util.List;

public class EngineParticle extends SingleQuadParticle {
    private final Identifier id;
    private final SpriteSet sprites;
    private final List<GenericParticleData> data;
    private final float baseQuadSize;
    private final Layer layer;

    public EngineParticle(Identifier id, ClientLevel level, double x, double y, double z, SpriteSet sprites, int lifetime, SingleQuadParticle.Layer layer, List<GenericParticleData> data) {
        super(level, x, y, z, sprites.first());
        this.id = id;
        this.sprites = sprites;
        this.data = data;
        this.baseQuadSize = this.quadSize;
        this.lifetime = lifetime;
        this.layer = layer;
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
        return layer;
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
        for (GenericParticleData d : data) {
            d.apply(this, age, this.lifetime);
        }
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final Identifier id;
        private final int lifetime;
        private final SingleQuadParticle.Layer layer;
        private final List<GenericParticleData> data;
        private final SpriteSet sprites;

        public Provider(Identifier id, int lifetime, SingleQuadParticle.Layer layer, List<GenericParticleData> data, SpriteSet sprites) {
            this.id = id;
            this.lifetime = lifetime;
            this.layer = layer;
            this.data = data;
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            EngineParticle particle = new EngineParticle(id, level, x, y, z, sprites, lifetime, layer, data);
            particle.setParticleSpeed(xSpeed, ySpeed, zSpeed);
            return particle;
        }
    }
}