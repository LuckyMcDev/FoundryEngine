package de.luckymcdev.foundryengine.particle.client.data;

import de.luckymcdev.foundryengine.util.common.color.Color;

public interface ParticleContext {
	void applyColor(Color color);

	void applyScale(float scale);

	void setParticleSpeed(double x, double y, double z);

	void setPos(double x, double y, double z);

	void applyRotation(float radians);
}