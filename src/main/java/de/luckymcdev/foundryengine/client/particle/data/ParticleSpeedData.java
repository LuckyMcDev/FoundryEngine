package de.luckymcdev.foundryengine.client.particle.data;

import org.joml.Vector3d;

public final class ParticleSpeedData extends KeyframedParticleData<Vector3d> {
	public ParticleSpeedData(KeyframeSequence<Vector3d> sequence) {
		super(sequence);
	}

	@Override
	protected Vector3d interpolate(Vector3d start, Vector3d end, float easedProgress) {
		return new Vector3d(
			start.x + (end.x - start.x) * easedProgress,
			start.y + (end.y - start.y) * easedProgress,
			start.z + (end.z - start.z) * easedProgress
		);
	}

	@Override
	protected void applyValue(ParticleContext particle, Vector3d value) {
		particle.setParticleSpeed(value.x, value.y, value.z);
	}
}