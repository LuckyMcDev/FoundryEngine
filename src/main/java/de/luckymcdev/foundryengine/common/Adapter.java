package de.luckymcdev.foundryengine.common;

import de.luckymcdev.foundryengine.common.exeptions.UtilityClassException;

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
