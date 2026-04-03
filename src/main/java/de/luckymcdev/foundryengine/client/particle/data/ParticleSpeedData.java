package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.client.particle.AbstractEngineParticle;
import de.luckymcdev.foundryengine.common.easing.Easing;
import org.joml.Vector3d;

public record ParticleSpeedData(Vector3d startSpeed, Vector3d endSpeed, Easing easing) implements GenericParticleData {
    @Override
    public void apply(AbstractEngineParticle particle, int age, int lifetime) {
        Vector3d speed = speedForAge(age, lifetime);
        particle.setParticleSpeed(speed.x, speed.y, speed.z);
    }

    public Vector3d speedForAge(int age, int lifetime) {
        Easing resolvedEasing = easing == null ? Easing.LINEAR : easing;
        double eased = resolvedEasing.clamped(age, 0.0, 1.0, Math.max(1, lifetime));
        double x = startSpeed.x + (endSpeed.x - startSpeed.x) * eased;
        double y = startSpeed.y + (endSpeed.y - startSpeed.y) * eased;
        double z = startSpeed.z + (endSpeed.z - startSpeed.z) * eased;
        return new Vector3d(x, y, z);
    }
}
