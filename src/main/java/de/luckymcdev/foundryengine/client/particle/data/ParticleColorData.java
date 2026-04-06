package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.common.util.color.Color;

public final class ParticleColorData extends KeyframedParticleData<Color> {
    public ParticleColorData(KeyframeSequence<Color> sequence) {
        super(sequence);
    }

    @Override
    protected Color interpolate(Color start, Color end, float easedProgress) {
        return start.lerp(easedProgress, end);
    }

    @Override
    protected void applyValue(ParticleContext particle, Color value) {
        particle.applyColor(value);
    }
}