package de.luckymcdev.foundryengine.particle.client;

import net.minecraft.client.particle.SingleQuadParticle;

public enum ParticleLayer {
	OPAQUE, TRANSLUCENT;

	public SingleQuadParticle.Layer toMinecraft() {
		return switch (this) {
			case OPAQUE -> SingleQuadParticle.Layer.OPAQUE;
			case TRANSLUCENT -> SingleQuadParticle.Layer.TRANSLUCENT;
		};
	}
}
