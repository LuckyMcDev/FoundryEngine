package de.luckymcdev.foundryengine.script.common;

/**
 * Thrown when a script operation exceeds its configured execution timeout.
 */
public class ScriptTimeoutException extends RuntimeException {

	public ScriptTimeoutException(String description, long timeoutSeconds) {
		super("Script '" + description + "' timed out after " + timeoutSeconds + " seconds");
	}
}
