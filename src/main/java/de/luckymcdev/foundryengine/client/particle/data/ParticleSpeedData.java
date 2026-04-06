package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.common.easing.Easing;
import org.joml.Vector3d;

public final class ParticleSpeedData extends EasedGenericParticleData<Vector3d> {

    public ParticleSpeedData(Vector3d speed) {
        this(speed, speed);
    }

    public ParticleSpeedData(Vector3d startSpeed, Vector3d endSpeed) {
        this(startSpeed, endSpeed, Easing.LINEAR);
    }

    public ParticleSpeedData(Vector3d startSpeed, Vector3d endSpeed, Easing easing) {
        super(startSpeed, endSpeed, easing);
    }

    @Override
    protected Vector3d interpolate(float progress) {
        return new Vector3d(
                start.x + (end.x - start.x) * progress,
                start.y + (end.y - start.y) * progress,
                start.z + (end.z - start.z) * progress
        );
    }

    @Override
    protected void applyValue(ParticleContext particle, Vector3d value) {
        particle.setParticleSpeed(value.x, value.y, value.z);
    }
}
