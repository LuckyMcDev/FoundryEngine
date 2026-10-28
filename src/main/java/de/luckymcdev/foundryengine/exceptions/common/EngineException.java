package de.luckymcdev.foundryengine.exceptions.common;

/**
 * Base runtime exception for Foundry Engine errors.
 */
public class EngineException extends RuntimeException {

	public EngineException() {
		super();
	}

	public EngineException(String message) {
		super(message);
	}

	public EngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public EngineException(Throwable cause) {
		super(cause);
	}
}
