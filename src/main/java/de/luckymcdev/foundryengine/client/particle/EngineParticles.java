package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.event.RegistryEvent;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.particle.data.ParticleColorData;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.particles.SimpleParticleType;

public class EngineParticles {
    public static final ParticleBuilder BUILDER = ParticleBuilder.create(Common.id("engine_particle"))
            .position(0, 100, 0)
            .velocity(0, 0.1f, 0)
            .addData(new ParticleColorData(Color.DARK_GRAY, Color.PINK, Easing.SINE_IN));

    public static void register(RegistryEvent event) {
        event.particles(BUILDER);
    }

    public static SimpleParticleType type() {
        return (SimpleParticleType) BUILDER.get();
    }

    public static void tick() {
        Client.getParticleManager().spawn(BUILDER);
    }
}
