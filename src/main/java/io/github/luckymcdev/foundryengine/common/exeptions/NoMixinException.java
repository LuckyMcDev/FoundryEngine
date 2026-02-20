package io.github.luckymcdev.foundryengine.common.exeptions;

/**
 * An Exception which is thrown when a Mixin should have implemented a Method but hasn't.
 * {@link io.github.luckymcdev.foundryengine.interfaces}
 */
public class NoMixinException extends IllegalStateException {
    /**
     * Constructs a new {@link NoMixinException}
     *
     * @param thisObject object whose method should have been implemented.
     */
    public NoMixinException(Object thisObject) {
        super("A mixin should have implemented this method! Missing in " + thisObject.getClass().getName());
    }
}
