package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.client.particle.EngineParticle;
import de.luckymcdev.foundryengine.common.easing.Easing;
import de.luckymcdev.foundryengine.common.util.color.Color;

public final class ParticleColorData extends EasedGenericParticleData<Color> {

    public ParticleColorData(Color startColor, Color endColor) {
        this(startColor, endColor, Easing.LINEAR);
    }

    public ParticleColorData(Color startColor, Color endColor, Easing easing) {
        super(startColor, endColor, easing);
    }

    @Override
    protected Color interpolate(float progress) {
        return start.lerp(progress, end);
    }

    @Override
    protected void applyValue(EngineParticle particle, Color value) {
        particle.applyColor(value);
    }
}
