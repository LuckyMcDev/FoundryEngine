package de.luckymcdev.foundryengine.common;

import de.luckymcdev.foundryengine.common.exceptions.UtilityClassException;

/**
 * Utility adapter for converting objects to Foundry Engine types.
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
