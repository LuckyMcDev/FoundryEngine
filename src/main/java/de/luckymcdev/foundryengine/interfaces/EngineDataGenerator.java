package de.luckymcdev.foundryengine.interfaces;

public interface EngineDataGenerator {
    default boolean shouldSkipPurging() {
        return true;
    }
}
