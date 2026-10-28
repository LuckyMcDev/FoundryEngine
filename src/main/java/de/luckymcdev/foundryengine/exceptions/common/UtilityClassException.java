package de.luckymcdev.foundryengine.exceptions.common;

/**
 * Thrown when a utility class is instantiated via its private constructor.
 */
public class UtilityClassException extends UnsupportedOperationException {
	public UtilityClassException() {
		super("This is a Utility Class and should not be Instantiated");
	}
}
