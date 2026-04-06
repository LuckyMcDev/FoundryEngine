package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.common.easing.Easing;
import org.joml.Vector3d;

public final class ParticlePositionData extends EasedGenericParticleData<Vector3d> {

    public ParticlePositionData(Vector3d position) {
        this(position, position);
    }

    public ParticlePositionData(Vector3d startPos, Vector3d endPos) {
        this(startPos, endPos, Easing.LINEAR);
    }

    public ParticlePositionData(Vector3d startPos, Vector3d endPos, Easing easing) {
        super(startPos, endPos, easing);
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
        particle.setPos(value.x, value.y, value.z);
    }
}
