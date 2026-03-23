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

    public boolean frozen() {
        return frozen;
    }

    public void freeze() {
        this.frozen = true;
    }

    public void setValue(T value) {
        if (frozen()) return;
        this.value = value;
    }

    public T value() {
        return value;
    }
}
