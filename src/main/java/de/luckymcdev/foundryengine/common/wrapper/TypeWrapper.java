package de.luckymcdev.foundryengine.common.wrapper;

/**
 * Converts string input to a typed value.
 */
public interface TypeWrapper<T> {
    T wrap(String input);
}
