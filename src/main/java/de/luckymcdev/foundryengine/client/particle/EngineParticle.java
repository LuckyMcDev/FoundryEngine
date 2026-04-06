package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleLayer;
import de.luckymcdev.foundryengine.client.particle.data.GenericParticleData;
import de.luckymcdev.foundryengine.client.particle.data.ParticleContext;
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

/**
 * Implements {@link ParticleContext} so that {@link GenericParticleData} implementations
 * can call particle methods without importing any net.minecraft.client types.
 */
public class EngineParticle extends SingleQuadParticle implements ParticleContext {
    private final Identifier id;
    private final SpriteSet sprites;
    private final List<GenericParticleData> data;
    private final float baseQuadSize;
    private final Layer layer;

    public EngineParticle(Identifier id, ClientLevel level, double x, double y, double z,
                          SpriteSet sprites, int lifetime, ParticleLayer layer,
                          List<GenericParticleData> data) {
        super(level, x, y, z, sprites.first());
        this.id = id;
        this.sprites = sprites;
        this.data = data;
        this.baseQuadSize = this.quadSize;
        this.lifetime = lifetime;
        this.layer = layer.toMinecraft();
        applyData(0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) return;
        setSpriteFromAge(sprites);
        applyData(this.age);
    }

    @Override
    protected Layer getLayer() {
        return layer;
    }


    @Override
    public void applyColor(Color color) {
        setColor(color.r(), color.g(), color.b());
        setAlpha(color.a());
    }

    @Override
    public void applyScale(float scale) {
        this.quadSize = this.baseQuadSize * scale;
        this.setSize(0.2F * scale, 0.2F * scale);
    }

    @Override
    public void setParticleSpeed(double x, double y, double z) {
        super.setParticleSpeed(x, y, z);
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
    }

    private void applyData(int age) {
        for (GenericParticleData d : data) {
            d.apply(this, age, this.lifetime);
        }
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final Identifier id;
        private final int lifetime;
        private final ParticleLayer layer;
        private final List<GenericParticleData> data;
        private final SpriteSet sprites;

        public Provider(Identifier id, int lifetime, ParticleLayer layer,
                        List<GenericParticleData> data, SpriteSet sprites) {
            this.id = id;
            this.lifetime = lifetime;
            this.layer = layer;
            this.data = data;
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource random) {
            EngineParticle particle = new EngineParticle(id, level, x, y, z,
                    sprites, lifetime, layer, data);
            particle.setParticleSpeed(xSpeed, ySpeed, zSpeed);
            return particle;
        }
    }
}