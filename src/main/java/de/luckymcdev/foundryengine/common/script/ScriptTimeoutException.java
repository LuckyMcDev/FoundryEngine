package de.luckymcdev.foundryengine.common.script;

/**
 * Thrown when a script operation exceeds its configured execution timeout.
 */
public class ScriptTimeoutException extends RuntimeException {

	public ScriptTimeoutException(String description, long timeoutSeconds) {
		super("Script '" + description + "' timed out after " + timeoutSeconds + " seconds");
	}
}
