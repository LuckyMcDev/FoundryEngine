package io.github.luckymcdev.foundryengine.common.util;

/**
 * {@link java.util.function.BiConsumer}
 */
@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void accept(A a, B b, C c, D d);
}
