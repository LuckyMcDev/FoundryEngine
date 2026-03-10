package io.github.luckymcdev.foundryengine.common;

/**
 * HEAVY WIP, May sometime be to adapt things to Foundry Engine objects.
 */
public class Adapter {
    @SuppressWarnings("unchecked")
    private static <O> O adapt(Object obj) {
        return (O) obj;
    }
}
