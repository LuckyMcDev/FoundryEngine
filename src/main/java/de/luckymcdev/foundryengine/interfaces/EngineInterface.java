package de.luckymcdev.foundryengine.interfaces;

/**
 * Base interface for all engine mixin interfaces, providing a self-cast helper.
 *
 * @param <T> the target type this interface wraps
 */
public interface EngineInterface<T> {
	@SuppressWarnings("unchecked")
	default T engine$self() {
		return (T) this;
	}
}
