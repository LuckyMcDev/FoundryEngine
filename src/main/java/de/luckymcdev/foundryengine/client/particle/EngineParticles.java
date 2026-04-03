package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.client.particle.data.ParticleColorData;
import de.luckymcdev.foundryengine.client.particle.data.ParticleScaleData;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EngineParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Common.MODID);

    private static final List<ParticleRegistration> SIMPLE_PARTICLES = new ArrayList<>();

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ENGINE_PARTICLE = registerSimple(
            "engine_particle",
            EngineParticleSpec.builder()
                    .lifetime(20)
                    .layer(SingleQuadParticle.Layer.OPAQUE)
                    .addData(new ParticleScaleData(10.0f, 10.0f, Easing.LINEAR))
                    .addData(new ParticleColorData(new Color(255, 0, 0), new Color(255, 0, 0), Easing.LINEAR))
                    .build()
    );

    public static List<ParticleRegistration> simpleParticles() {
        return Collections.unmodifiableList(SIMPLE_PARTICLES);
    }

    public static DeferredHolder<ParticleType<?>, SimpleParticleType> registerSimple(String name, EngineParticleSpec spec) {
        DeferredHolder<ParticleType<?>, SimpleParticleType> holder =
                PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
        SIMPLE_PARTICLES.add(new ParticleRegistration(holder, spec));
        return holder;
    }

    public record ParticleRegistration(DeferredHolder<ParticleType<?>, SimpleParticleType> type,
                                       EngineParticleSpec spec) {
    }
}
