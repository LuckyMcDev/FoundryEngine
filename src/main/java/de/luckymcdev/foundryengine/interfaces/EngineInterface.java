package de.luckymcdev.foundryengine.interfaces;

public interface EngineInterface<T> {
    @SuppressWarnings("unchecked")
    default T engine$self() {
        return (T) this;
    }
}
