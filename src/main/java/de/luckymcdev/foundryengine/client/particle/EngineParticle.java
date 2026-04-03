package de.luckymcdev.foundryengine.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;

public class EngineParticle extends AbstractEngineParticle {
    public EngineParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, EngineParticleSpec spec) {
        super(level, x, y, z, sprites, spec);
    }
}
