package de.luckymcdev.foundryengine.common.builder.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.client.particle.EngineParticleSpec;
import de.luckymcdev.foundryengine.client.particle.data.GenericParticleData;
import de.luckymcdev.foundryengine.common.builder.BuilderBaseImpl;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Particle builder implementation for registering ParticleTypes.
 */
public class ParticleBuilderImpl extends BuilderBaseImpl<ParticleType<?>> implements ParticleBuilder {
    private final List<GenericParticleData> data;
    private boolean alwaysShow;
    private Function<Boolean, ParticleType<?>> factory;
    private int lifetime;
    private SingleQuadParticle.Layer layer;
    private Vector3f position;
    private Vector3f velocity;

    public ParticleBuilderImpl(Identifier id) {
        super(id);
        this.registryKey = Registries.PARTICLE_TYPE;
        this.alwaysShow = false;
        this.factory = SimpleParticleType::new;
        this.lifetime = 20;
        this.layer = SingleQuadParticle.Layer.OPAQUE;
        this.data = new ArrayList<>();
        this.position = new Vector3f(0, 0, 0);
        this.velocity = new Vector3f(0, 0, 0);
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
    public ParticleBuilder spec(EngineParticleSpec spec) {
        this.lifetime = spec.lifetime();
        this.layer = spec.layer();
        this.data.clear();
        this.data.addAll(spec.data());
        return this;
    }

    @Override
    public ParticleBuilder lifetime(int lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    @Override
    public ParticleBuilder position(Vector3f position) {
        this.position = position;
        return this;
    }

    @Override
    public ParticleBuilder position(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
        return this;
    }

    @Override
    public ParticleBuilder velocity(Vector3f velocity) {
        this.velocity = velocity;
        return this;
    }

    @Override
    public ParticleBuilder velocity(float x, float y, float z) {
        this.velocity = new Vector3f(x, y, z);
        return this;
    }

    @Override
    public ParticleBuilder layer(SingleQuadParticle.Layer layer) {
        this.layer = layer;
        return this;
    }

    @Override
    public ParticleBuilder addData(GenericParticleData data) {
        this.data.add(data);
        return this;
    }

    @Override
    public ParticleBuilder data(GenericParticleData... data) {
        this.data.addAll(List.of(data));
        return this;
    }

    @Override
    public ParticleBuilder data(List<GenericParticleData> data) {
        this.data.addAll(data);
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

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public EngineParticleSpec getSpec() {
        return new EngineParticleSpec(lifetime, layer, data);
    }
}
