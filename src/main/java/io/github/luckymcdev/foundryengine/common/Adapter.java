package io.github.luckymcdev.foundryengine.common;

import io.github.luckymcdev.foundryengine.common.exeptions.UtilityClassException;

/**
 * HEAVY WIP, May sometime be to adapt things to Foundry Engine objects.
 */
public class Adapter {
    private Adapter() {
        throw new UtilityClassException();
    }

    @SuppressWarnings("unchecked")
    private static <O> O adapt(Object obj) {
        return (O) obj;
    }
}
