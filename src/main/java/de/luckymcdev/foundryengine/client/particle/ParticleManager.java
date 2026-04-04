package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilderImpl;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Vector3d;

public class ParticleManager {

    /**
     * Spawns a particle at the given position with no initial velocity.
     * Any velocity over the particle's lifetime should be configured via
     * {@link de.luckymcdev.foundryengine.client.particle.data.ParticleVelocityData} on the builder.
     */
    public void spawn(ParticleBuilder builder, double x, double y, double z) {
        spawn(builder, x, y, z, 0, 0, 0);
    }

    /**
     * Spawns a particle at the given position with an explicit initial velocity.
     * Note: if the builder also has {@link de.luckymcdev.foundryengine.client.particle.data.ParticleVelocityData},
     * that will override this velocity each tick via {@link EngineParticle#tick()}.
     */
    public void spawn(ParticleBuilder builder, double x, double y, double z, double vx, double vy, double vz) {
        ParticleBuilderImpl impl = (ParticleBuilderImpl) builder;
        Particle particle = Client.getMinecraft().particleEngine.createParticle(
                (SimpleParticleType) impl.get(),
                x, y, z,
                vx, vy, vz
        );
        if (particle != null) {
            Client.getMinecraft().particleEngine.add(particle);
        }
    }

    /**
     * Convenience overload accepting {@link Vector3d} for position.
     */
    public void spawn(ParticleBuilder builder, Vector3d position) {
        spawn(builder, position.x, position.y, position.z);
    }

    /**
     * Convenience overload accepting {@link Vector3d} for both position and initial velocity.
     */
    public void spawn(ParticleBuilder builder, Vector3d position, Vector3d initialVelocity) {
        spawn(builder, position.x, position.y, position.z, initialVelocity.x, initialVelocity.y, initialVelocity.z);
    }
}