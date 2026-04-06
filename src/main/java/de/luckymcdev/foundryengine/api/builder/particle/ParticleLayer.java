package de.luckymcdev.foundryengine.api.builder.particle;

public enum ParticleLayer {
    OPAQUE,
    TRANSLUCENT;

    public net.minecraft.client.particle.SingleQuadParticle.Layer toMinecraft() {
        return switch (this) {
            case OPAQUE -> net.minecraft.client.particle.SingleQuadParticle.Layer.OPAQUE;
            case TRANSLUCENT -> net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT;
        };
    }
}