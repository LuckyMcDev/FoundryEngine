package de.luckymcdev.foundryengine.common.exceptions;

/**
 * An Exception which is thrown when a Mixin should have implemented a Method but hasn't.
 * {@link de.luckymcdev.foundryengine.interfaces}
 */
public class NoMixinException extends IllegalStateException {
	/**
	 * Constructs a new
	 *
	 * @param thisObject object whose method should have been implemented.
	 */
	public NoMixinException(Object thisObject) {
		super("A mixin should have implemented this method! Missing in " + thisObject.getClass().getName());
	}
}
