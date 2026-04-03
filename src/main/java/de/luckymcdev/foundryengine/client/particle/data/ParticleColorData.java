package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.client.particle.AbstractEngineParticle;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;

public record ParticleColorData(Color startColor, Color endColor, Easing easing) implements GenericParticleData {

    public ParticleColorData(Color startColor, Color endColor) {
        this(startColor, endColor, Easing.LINEAR);
    }

    @Override
    public void apply(AbstractEngineParticle particle, int age, int lifetime) {
        Color color = colorForAge(age, lifetime);
        particle.applyColor(color);
    }

    public Color colorForAge(int age, int lifetime) {
        float eased = easing.clamped(age, 0.0f, 1.0f, Math.max(1, lifetime));
        return startColor.lerp(eased, endColor);
    }
}
