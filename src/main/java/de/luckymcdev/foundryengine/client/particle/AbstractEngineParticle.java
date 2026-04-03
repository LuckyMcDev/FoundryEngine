package de.luckymcdev.foundryengine.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;

public abstract class AbstractEngineParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final EngineParticleSpec spec;

    protected AbstractEngineParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, EngineParticleSpec spec) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.spec = spec;
        this.lifetime = spec.lifetime();
        scale(spec.scale());
        setColor(spec.red(), spec.green(), spec.blue());
        setAlpha(spec.alpha());
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);
    }

    @Override
    protected Layer getLayer() {
        return spec.layer();
    }
}
