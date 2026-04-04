package de.luckymcdev.foundryengine.common.builder.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.client.particle.data.*;
import de.luckymcdev.foundryengine.common.builder.BuilderBaseImpl;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Particle builder implementation for registering ParticleTypes.
 *
 * <p>Data modifiers are stored in four typed lists (color, scale, velocity, position)
 * plus a generic catch-all list for custom {@link GenericParticleData} implementations.
 * All five lists are merged when {@link #mergedData()} is called so that
 * {@link de.luckymcdev.foundryengine.client.particle.EngineParticle} receives a
 * single flat list, preserving insertion order within each category.</p>
 */
public class ParticleBuilderImpl extends BuilderBaseImpl<ParticleType<?>> implements ParticleBuilder {
    private final List<ParticleColorData> colorData = new ArrayList<>();
    private final List<ParticleScaleData> scaleData = new ArrayList<>();
    private final List<ParticleVelocityData> velocityData = new ArrayList<>();
    private final List<ParticlePositionData> positionData = new ArrayList<>();
    private final List<GenericParticleData> genericData = new ArrayList<>();

    private boolean alwaysShow;
    private Function<Boolean, ParticleType<?>> factory;
    private int lifetime;
    private SingleQuadParticle.Layer layer;

    public ParticleBuilderImpl(Identifier id) {
        super(id);
        this.registryKey = Registries.PARTICLE_TYPE;
        this.alwaysShow = false;
        this.factory = SimpleParticleType::new;
        this.lifetime = 20;
        this.layer = SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public ParticleBuilder factory(Function<Boolean, ParticleType<?>> factory) {
        this.factory = factory;
        return this;
    }

    @Override
    public ParticleBuilder alwaysShow() {
        return alwaysShow(true);
    }

    @Override
    public ParticleBuilder alwaysShow(boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
        return this;
    }

    @Override
    public ParticleBuilder lifetime(int lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    @Override
    public ParticleBuilder layer(SingleQuadParticle.Layer layer) {
        this.layer = layer;
        return this;
    }

    @Override
    public ParticleBuilder addColorData(ParticleColorData data) {
        colorData.add(data);
        return this;
    }

    @Override
    public ParticleBuilder addScaleData(ParticleScaleData data) {
        scaleData.add(data);
        return this;
    }

    @Override
    public ParticleBuilder addVelocityData(ParticleVelocityData data) {
        velocityData.add(data);
        return this;
    }

    @Override
    public ParticleBuilder addPositionData(ParticlePositionData data) {
        positionData.add(data);
        return this;
    }

    @Override
    public ParticleBuilder addData(GenericParticleData data) {
        switch (data) {
            case ParticleColorData d -> colorData.add(d);
            case ParticleScaleData d -> scaleData.add(d);
            case ParticleVelocityData d -> velocityData.add(d);
            case ParticlePositionData d -> positionData.add(d);
            default -> genericData.add(data);
        }
        return this;
    }

    @Override
    public ParticleBuilder data(GenericParticleData... data) {
        for (GenericParticleData d : data) addData(d);
        return this;
    }

    @Override
    public ParticleType<?> register(RegisterEvent.RegisterHelper<ParticleType<?>> helper) {
        ParticleType<?> type = build();
        helper.register(this.id, type);
        this.object = type;
        return type;
    }

    @Override
    public ParticleType<?> build() {
        return factory.apply(alwaysShow);
    }

    public int getLifetime() {
        return lifetime;
    }

    public SingleQuadParticle.Layer getLayer() {
        return layer;
    }

    /**
     * Returns a merged flat list of all data in order:
     * color → scale → velocity → position → generic.
     */
    public List<GenericParticleData> mergedData() {
        List<GenericParticleData> merged = new ArrayList<>();
        merged.addAll(colorData);
        merged.addAll(scaleData);
        merged.addAll(velocityData);
        merged.addAll(positionData);
        merged.addAll(genericData);
        return merged;
    }

    /**
     * Read-only views of each typed list (useful for tooling / serialization).
     */
    public List<ParticleColorData> getColorData() {
        return List.copyOf(colorData);
    }

    public List<ParticleScaleData> getScaleData() {
        return List.copyOf(scaleData);
    }

    public List<ParticleVelocityData> getVelocityData() {
        return List.copyOf(velocityData);
    }

    public List<ParticlePositionData> getPositionData() {
        return List.copyOf(positionData);
    }

    public List<GenericParticleData> getGenericData() {
        return List.copyOf(genericData);
    }

    private void clearAllData() {
        colorData.clear();
        scaleData.clear();
        velocityData.clear();
        positionData.clear();
        genericData.clear();
    }
}