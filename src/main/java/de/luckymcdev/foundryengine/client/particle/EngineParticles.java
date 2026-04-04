package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.event.RegistryEvent;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.particle.data.ParticleColorData;
import de.luckymcdev.foundryengine.client.particle.data.ParticlePositionData;
import de.luckymcdev.foundryengine.client.particle.data.ParticleScaleData;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Vector3d;

public class EngineParticles {
    public static final ParticleBuilder BUILDER = ParticleBuilder.create(Common.id("engine_particle"))
            .addPositionData(new ParticlePositionData(new Vector3d(0, 100, 0), new Vector3d(0, 110, 0)))
            .addScaleData(new ParticleScaleData(1, 10))
            .addColorData(new ParticleColorData(Color.DARK_GRAY, Color.PINK, Easing.SINE_IN));

    public static void register(RegistryEvent event) {
        event.particles(BUILDER);
    }

    public static SimpleParticleType type() {
        return (SimpleParticleType) BUILDER.get();
    }

    public static void tick() {
        Client.getParticleManager().spawn(BUILDER, new Vector3d(0, 100, 0));
    }
}
