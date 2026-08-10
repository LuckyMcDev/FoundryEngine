package de.luckymcdev.foundryengine.client.particle;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Vector3d;

public class ParticleManager {

	/**
	 * Spawns a particle at the position provided by the first keyframe of the position data.
	 */
	public void spawn(ParticleBuilder builder) {
		spawn(builder, builder.startPosition(), builder.startVelocity());
	}

	public void spawn(ParticleBuilder builder, double x, double y, double z) {
		spawn(builder, x, y, z, 0, 0, 0);
	}

	public void spawn(ParticleBuilder builder, double x, double y, double z, double vx, double vy, double vz) {

		// Use the Minecraft particle engine to create the instance
		Particle particle = Minecraft.getInstance().particleEngine.createParticle(
			(SimpleParticleType) builder.get(),
			x, y, z,
			vx, vy, vz
		);

		if (particle != null) {
			Minecraft.getInstance().particleEngine.add(particle);
		}
	}

	public void spawn(ParticleBuilder builder, Vector3d position) {
		spawn(builder, position.x, position.y, position.z);
	}

	public void spawn(ParticleBuilder builder, Vector3d position, Vector3d initialVelocity) {
		spawn(builder, position.x, position.y, position.z, initialVelocity.x, initialVelocity.y, initialVelocity.z);
	}
}