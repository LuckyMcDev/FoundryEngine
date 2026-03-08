package io.github.luckymcdev.foundryengine.common.util;

@FunctionalInterface
public interface TriConsumer<A, B, C> {
    void accept(A a, B b, C c);
}

