package de.luckymcdev.foundryengine.wrapper.common;

/**
 * Converts string input to a typed value.
 */
public interface TypeWrapper<T> {
	T wrap(String input);
}
