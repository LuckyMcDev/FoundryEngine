package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilderImpl;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Vector3d;

public class ParticleManager {

    /**
     * Spawns a particle at the position provided by the first keyframe of the position data.
     */
    public void spawn(ParticleBuilder builder) {
        ParticleBuilderImpl impl = (ParticleBuilderImpl) builder;

        Vector3d startPos = impl.getPositionData().getSequence().getFirstValue();
        Vector3d startVel = impl.getVelocityData().getSequence().getFirstValue();

        if (startPos == null) startPos = new Vector3d(0, 0, 0);
        if (startVel == null) startVel = new Vector3d(0, 0, 0);

        spawn(builder, startPos, startVel);
    }

    public void spawn(ParticleBuilder builder, double x, double y, double z) {
        spawn(builder, x, y, z, 0, 0, 0);
    }

    public void spawn(ParticleBuilder builder, double x, double y, double z, double vx, double vy, double vz) {
        ParticleBuilderImpl impl = (ParticleBuilderImpl) builder;

        // Use the Minecraft particle engine to create the instance
        Particle particle = Client.getMc().particleEngine.createParticle(
                (SimpleParticleType) impl.get(),
                x, y, z,
                vx, vy, vz
        );

        if (particle != null) {
            Client.getMc().particleEngine.add(particle);
        }
    }

    public void spawn(ParticleBuilder builder, Vector3d position) {
        spawn(builder, position.x, position.y, position.z);
    }

    public void spawn(ParticleBuilder builder, Vector3d position, Vector3d initialVelocity) {
        spawn(builder, position.x, position.y, position.z, initialVelocity.x, initialVelocity.y, initialVelocity.z);
    }
}