package de.luckymcdev.foundryengine.common.builder.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.builder.particle.ParticleLayer;
import de.luckymcdev.foundryengine.client.particle.data.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ParticleBuilderImpl implements ParticleBuilder {
    private final Identifier id;
    private ParticleColorData colorData;
    private ParticleScaleData scaleData;
    private ParticleVelocityData velocityData;
    private ParticlePositionData positionData;
    private ParticleRotationData rotationData;
    private boolean alwaysShow = false;
    private Function<Boolean, ParticleType<?>> factory = SimpleParticleType::new;
    private int lifetime = 20;
    private ParticleLayer layer = ParticleLayer.OPAQUE;
    private @Nullable ParticleType<?> object;

    public ParticleBuilderImpl(Identifier id) {
        this.id = id;
    }

    @Override
    public ParticleBuilder colorData(ParticleColorData data) {
        this.colorData = data;
        return this;
    }

    @Override
    public ParticleBuilder scaleData(ParticleScaleData data) {
        this.scaleData = data;
        return this;
    }

    @Override
    public ParticleBuilder velocityData(ParticleVelocityData data) {
        this.velocityData = data;
        return this;
    }

    @Override
    public ParticleBuilder positionData(ParticlePositionData data) {
        this.positionData = data;
        return this;
    }

    @Override
    public ParticleBuilder rotationData(ParticleRotationData data) {
        this.rotationData = data;
        return this;
    }

    @Override
    public ParticleBuilder factory(Function<Boolean, ParticleType<?>> factory) {
        this.factory = factory;
        return this;
    }

    @Override
    public ParticleBuilder alwaysShow() {
        this.alwaysShow = true;
        return this;
    }

    @Override
    public ParticleBuilder lifetime(int lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    @Override
    public ParticleBuilder layer(ParticleLayer layer) {
        this.layer = layer;
        return this;
    }

    /**
     * Returns a merged list of active data for the EngineParticle.
     */
    public List<GenericParticleData> mergedData() {
        List<GenericParticleData> merged = new ArrayList<>();
        if (colorData != null) merged.add(colorData);
        if (scaleData != null) merged.add(scaleData);
        if (velocityData != null) merged.add(velocityData);
        if (positionData != null) merged.add(positionData);
        if (rotationData != null) merged.add(rotationData);
        return merged;
    }

    public ParticleColorData getColorData() {
        return colorData;
    }

    public ParticleScaleData getScaleData() {
        return scaleData;
    }

    public ParticleVelocityData getVelocityData() {
        return velocityData;
    }

    public ParticlePositionData getPositionData() {
        return positionData;
    }

    public ParticleRotationData getRotationData() {
        return rotationData;
    }

    public int getLifetime() {
        return lifetime;
    }

    public ParticleLayer getLayer() {
        return layer;
    }

    @Override
    public ParticleType<?> register(RegisterEvent.RegisterHelper<ParticleType<?>> helper) {
        ParticleType<?> type = build();
        helper.register(id, type);
        this.object = type;
        return type;
    }

    @Override
    public ParticleType<?> build() {
        return factory.apply(alwaysShow);
    }

    @Override
    public ParticleType<?> get() {
        if (object == null) {
            throw new IllegalStateException("Particle " + id + " has not been registered yet");
        }
        return object;
    }

    @Override
    public ParticleType<?> getOrCreate() {
        if (object == null) {
            object = build();
        }
        return object;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Identifier newID(String pre, String post) {
        if (pre.isEmpty() && post.isEmpty()) {
            return id;
        }
        return id.withPath(pre + id.getPath() + post);
    }
}