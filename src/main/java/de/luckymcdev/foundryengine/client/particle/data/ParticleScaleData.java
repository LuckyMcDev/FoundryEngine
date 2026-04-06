package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.common.easing.Easing;

public final class ParticleScaleData extends EasedGenericParticleData<Float> {

    public ParticleScaleData(float scale) {
        this(scale, scale);
    }

    public ParticleScaleData(float startScale, float endScale) {
        this(startScale, endScale, Easing.LINEAR);
    }

    public ParticleScaleData(float startScale, float endScale, Easing easing) {
        super(startScale, endScale, easing);
    }

    @Override
    protected Float interpolate(float progress) {
        return start + (end - start) * progress;
    }

    @Override
    protected void applyValue(ParticleContext particle, Float value) {
        particle.applyScale(value);
    }
}
