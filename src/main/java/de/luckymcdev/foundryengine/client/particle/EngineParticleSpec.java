package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.client.particle.data.GenericParticleData;
import net.minecraft.client.particle.SingleQuadParticle;

import java.util.List;
import java.util.Objects;

public record EngineParticleSpec(int lifetime, SingleQuadParticle.Layer layer, List<GenericParticleData> data) {
    public EngineParticleSpec(List<GenericParticleData> data) {
        this(20, SingleQuadParticle.Layer.OPAQUE, data);
    }

    public EngineParticleSpec(int lifetime, SingleQuadParticle.Layer layer, List<GenericParticleData> data) {
        this.lifetime = lifetime;
        this.layer = Objects.requireNonNull(layer, "layer");
        this.data = List.copyOf(Objects.requireNonNull(data, "data"));
    }
}
