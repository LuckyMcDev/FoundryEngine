package io.github.luckymcdev.common.exeptions;

public class NoMixinException extends IllegalStateException {
    public NoMixinException(Object thisObject) {
        super("A mixin should have implemented this method! Missing in " + thisObject.getClass().getName());
    }
}
