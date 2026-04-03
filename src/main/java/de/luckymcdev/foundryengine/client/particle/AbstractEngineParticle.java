package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.client.particle.data.GenericParticleData;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;

public abstract class AbstractEngineParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final EngineParticleSpec spec;
    private final float baseQuadSize;

    protected AbstractEngineParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, EngineParticleSpec spec) {
        super(level, x, y, z, sprites.first());
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
}
