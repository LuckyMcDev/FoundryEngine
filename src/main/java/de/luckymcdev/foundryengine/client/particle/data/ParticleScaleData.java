package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.client.particle.AbstractEngineParticle;
import de.luckymcdev.foundryengine.common.easing.Easing;

public record ParticleScaleData(float startScale, float endScale, Easing easing) implements GenericParticleData {
    @Override
    public void apply(AbstractEngineParticle particle, int age, int lifetime) {
        float scale = scaleForAge(age, lifetime);
        particle.applyScale(scale);
    }

    public float scaleForAge(int age, int lifetime) {
        Easing resolvedEasing = easing == null ? Easing.LINEAR : easing;
        return resolvedEasing.clamped(age, startScale, endScale - startScale, Math.max(1, lifetime));
    }
}
