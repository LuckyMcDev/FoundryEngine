package de.luckymcdev.foundryengine.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;

public class EngineParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    public EngineParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.lifetime = 20;
        scale(10);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }
}
