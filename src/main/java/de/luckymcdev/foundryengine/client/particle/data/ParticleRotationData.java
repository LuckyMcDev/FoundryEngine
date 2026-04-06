package de.luckymcdev.foundryengine.client.particle.data;

public final class ParticleRotationData extends KeyframedParticleData<Float> {
    public ParticleRotationData(KeyframeSequence<Float> sequence) {
        super(sequence);
    }

    @Override
    protected Float interpolate(Float start, Float end, float easedProgress) {
        return start + (end - start) * easedProgress;
    }

    @Override
    protected void applyValue(ParticleContext particle, Float value) {
        particle.applyRotation(value);
    }
}