package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.event.RegistryEvent;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Vector3d;

public class EngineParticles {
    public static final ParticleBuilder BUILDER = ParticleBuilder.create(Common.id("engine_particle"))
            .position(new Vector3d(0, 100, 0))
            .lifetime(1).scale(1)
            .color(Color.DARK_GRAY, Color.PINK, Easing.SINE_IN)
            .velocity(new Vector3d(0, 0, 0));

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