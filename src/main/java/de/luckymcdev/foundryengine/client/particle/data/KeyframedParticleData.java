package de.luckymcdev.foundryengine.client.particle.data;

public abstract class KeyframedParticleData<T> implements GenericParticleData {
	protected final KeyframeSequence<T> sequence;

	protected KeyframedParticleData(KeyframeSequence<T> sequence) {
		this.sequence = sequence;
	}

	@Override
	public final void apply(ParticleContext particle, int age, int lifetime) {
		float progress = (float) age / (float) Math.max(1, lifetime);
		KeyframeSequence.InterpolationContext<T> ctx = sequence.getInterpolation(progress);

		if (ctx != null) {
			applyValue(particle, interpolate(ctx.start(), ctx.end(), ctx.eased()));
		}
	}

	public KeyframeSequence<T> getSequence() {
		return sequence;
	}

	protected abstract T interpolate(T start, T end, float easedProgress);

	protected abstract void applyValue(ParticleContext particle, T value);
}