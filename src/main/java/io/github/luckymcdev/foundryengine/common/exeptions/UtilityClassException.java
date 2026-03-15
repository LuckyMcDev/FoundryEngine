package io.github.luckymcdev.foundryengine.common.exeptions;

public class UtilityClassException extends UnsupportedOperationException {
    public UtilityClassException() {
        super("This is a Utility Class and should not be Instantiated");
    }
}
