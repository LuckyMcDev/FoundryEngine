package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.event.RegistryEvent;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.particle.data.ParticleColorData;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.SimpleParticleType;

public class EngineParticles {
    public static final ParticleBuilder BUILDER = ParticleBuilder.create(Common.id("engine_particle"))
            .addData(new ParticleColorData(Color.BLACK, Color.WHITE, Easing.SINE_IN));

    public static void register(RegistryEvent event) {
        event.particles(BUILDER);
    }

    public static SimpleParticleType type() {
        return (SimpleParticleType) BUILDER.get();
    }

    public static void tick() {
        Particle particle = Client.getMinecraft().particleEngine.createParticle(
                type(),
                0, 100, 0,
                0, 0.1, 0
        );
        if (particle != null) {
            Client.getMinecraft().particleEngine.add(particle);
        }
    }
}
