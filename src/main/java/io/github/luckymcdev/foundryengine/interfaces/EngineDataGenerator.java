package io.github.luckymcdev.foundryengine.interfaces;

public interface EngineDataGenerator {
    default boolean shouldSkipPurging() {
        return true;
    }
}
