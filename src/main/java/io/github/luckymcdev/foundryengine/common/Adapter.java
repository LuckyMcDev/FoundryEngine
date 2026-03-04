package io.github.luckymcdev.foundryengine.common;

public class Adapter {
    @SuppressWarnings("unchecked")
    private static <O> O adapt(Object obj) {
        return (O) obj;
    }
}
