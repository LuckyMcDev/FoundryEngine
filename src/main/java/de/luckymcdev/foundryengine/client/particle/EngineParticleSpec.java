package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.client.particle.data.GenericParticleData;
import net.minecraft.client.particle.SingleQuadParticle;

import java.util.ArrayList;
import java.util.List;

public final class EngineParticleSpec {
    private final int lifetime;
    private final SingleQuadParticle.Layer layer;
    private final List<GenericParticleData> data;

    private EngineParticleSpec(Builder builder) {
        this.lifetime = builder.lifetime;
        this.layer = builder.layer;
        this.data = List.copyOf(builder.data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public int lifetime() {
        return lifetime;
    }

    public SingleQuadParticle.Layer layer() {
        return layer;
    }

    public List<GenericParticleData> data() {
        return data;
    }

    public static final class Builder {
        private int lifetime = 20;
        private SingleQuadParticle.Layer layer = SingleQuadParticle.Layer.OPAQUE;
        private final List<GenericParticleData> data = new ArrayList<>();

        public Builder lifetime(int lifetime) {
            this.lifetime = lifetime;
            return this;
        }

        public Builder layer(SingleQuadParticle.Layer layer) {
            this.layer = layer;
            return this;
        }

        public Builder addData(GenericParticleData data) {
            this.data.add(data);
            return this;
        }

        public EngineParticleSpec build() {
            return new EngineParticleSpec(this);
        }
    }
}
