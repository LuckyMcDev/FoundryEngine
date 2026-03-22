package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EngineParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Common.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ENGINE_PARTICLE = PARTICLE_TYPES.register("engine_particle", () -> new SimpleParticleType(false));
}
