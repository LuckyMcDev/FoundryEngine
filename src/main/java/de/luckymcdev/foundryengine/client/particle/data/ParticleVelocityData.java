package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.client.particle.EngineParticle;
import de.luckymcdev.foundryengine.common.easing.Easing;
import org.joml.Vector3d;

public final class ParticleVelocityData extends EasedGenericParticleData<Vector3d> {

    public ParticleVelocityData(Vector3d velocity) {
        this(velocity, velocity, Easing.LINEAR);
    }

    public ParticleVelocityData(Vector3d startVelocity, Vector3d endVelocity) {
        this(startVelocity, endVelocity, Easing.LINEAR);
    }

    public ParticleVelocityData(Vector3d startVelocity, Vector3d endVelocity, Easing easing) {
        super(startVelocity, endVelocity, easing);
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
    protected void applyValue(EngineParticle particle, Vector3d value) {
        particle.setParticleSpeed(value.x, value.y, value.z);
    }
}
