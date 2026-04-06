package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.common.easing.Easing;

/**
 * Base class for particle data that interpolates a value over the particle's lifetime using an easing function.
 *
 * @param <T> The type of value being interpolated (e.g. Float, Vector3d, Color)
 */
public abstract class EasedGenericParticleData<T> implements GenericParticleData {
    protected final T start;
    protected final T end;
    protected final Easing easing;

    protected EasedGenericParticleData(T start, T end, Easing easing) {
        this.start = start;
        this.end = end;
        this.easing = easing;
    }

    /**
     * Returns the eased progress (0.0 to 1.0) for the given age and lifetime.
     */
    protected float easedProgress(int age, int lifetime) {
        return easing.clamped(age, 0.0f, 1.0f, Math.max(1, lifetime));
    }

    /**
     * Interpolates between start and end using the eased progress.
     */
    protected abstract T interpolate(float progress);

    /**
     * Returns the interpolated value for the given age and lifetime.
     */
    public T valueForAge(int age, int lifetime) {
        return interpolate(easedProgress(age, lifetime));
    }

    @Override
    public final void apply(ParticleContext particle, int age, int lifetime) {
        applyValue(particle, valueForAge(age, lifetime));
    }

    /**
     * Applies the interpolated value to the particle.
     */
    protected abstract void applyValue(ParticleContext particle, T value);

    public T getStart() {
        return start;
    }

    public T getEnd() {
        return end;
    }

    public Easing getEasing() {
        return easing;
    }
}
