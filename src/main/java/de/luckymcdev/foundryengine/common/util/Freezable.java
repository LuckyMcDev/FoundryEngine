package de.luckymcdev.foundryengine.common.util;

/**
 * A class which can handle a value which is freezable.
 * Very simple.
 *
 * @param <T> the type of value.
 */
public class Freezable<T> {
	private boolean frozen;
	private T value;

	public Freezable(T value) {
		this.value = value;
	}

	/**
	 * Returns true if this value is frozen (immutable).
	 */
	public boolean frozen() {
		return frozen;
	}

	/**
	 * Freezes this value, preventing further modifications.
	 */
	public void freeze() {
		this.frozen = true;
	}

	/**
	 * Sets the value if not frozen.
	 */
	public void setValue(T value) {
		if (frozen()) {
			return;
		}
		this.value = value;
	}

	/**
	 * Returns the current value.
	 */
	public T value() {
		return value;
	}
}
