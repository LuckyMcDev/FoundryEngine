package io.github.luckymcdev.foundryengine.common.util;

/**
 * {@link java.util.function.BiConsumer}
 */
@FunctionalInterface
public interface TriConsumer<A, B, C> {
    void accept(A a, B b, C c);
}

