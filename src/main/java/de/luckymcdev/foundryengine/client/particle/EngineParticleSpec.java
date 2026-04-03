package de.luckymcdev.foundryengine.client.particle;

import net.minecraft.client.particle.SingleQuadParticle;

public final class EngineParticleSpec {
    private final int lifetime;
    private final float scale;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final SingleQuadParticle.Layer layer;

    private EngineParticleSpec(Builder builder) {
        this.lifetime = builder.lifetime;
        this.scale = builder.scale;
        this.red = builder.red;
        this.green = builder.green;
        this.blue = builder.blue;
        this.alpha = builder.alpha;
        this.layer = builder.layer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int lifetime() {
        return lifetime;
    }

    public float scale() {
        return scale;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    public float alpha() {
        return alpha;
    }

    public SingleQuadParticle.Layer layer() {
        return layer;
    }

    public static final class Builder {
        private int lifetime = 20;
        private float scale = 1.0f;
        private float red = 1.0f;
        private float green = 1.0f;
        private float blue = 1.0f;
        private float alpha = 1.0f;
        private SingleQuadParticle.Layer layer = SingleQuadParticle.Layer.OPAQUE;

        public Builder lifetime(int lifetime) {
            this.lifetime = lifetime;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder color(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            return this;
        }

        public Builder alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public Builder layer(SingleQuadParticle.Layer layer) {
            this.layer = layer;
            return this;
        }

        public EngineParticleSpec build() {
            return new EngineParticleSpec(this);
        }
    }
}
