package io.github.luckymcdev.foundryengine.common.data;

import io.github.luckymcdev.foundryengine.interfaces.EngineDataGenerator;

/**
 * Thread-local context to track when an EngineDataGenerator is running.
 * This allows the DataGeneratorMixin to determine whether to skip purging.
 */
public class DataGeneratorContext {
    private static final ThreadLocal<EngineDataGenerator> CURRENT_GENERATOR = new ThreadLocal<>();

    public static void setEngineGenerator(EngineDataGenerator generator) {
        CURRENT_GENERATOR.set(generator);
    }

    public static EngineDataGenerator getCurrentGenerator() {
        return CURRENT_GENERATOR.get();
    }

    public static boolean isEngineGeneratorRunning() {
        return CURRENT_GENERATOR.get() != null;
    }

    public static void clear() {
        CURRENT_GENERATOR.remove();
    }
}