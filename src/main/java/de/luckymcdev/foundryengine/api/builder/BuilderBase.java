package de.luckymcdev.foundryengine.api.builder;

import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

/**
 * Base interface for all builders in the Foundry Engine API.
 * Provides common methods for state management.
 * Use composition (delegate to BuilderState) instead of inheritance.
 *
 * @param <T> The type of object this builder creates
 */
public interface BuilderBase<T> extends Supplier<T> {

    /**
     * Builds the object without registering it.
     *
     * @return The built object
     */
    T build();

    /**
     * Gets the object if it has been registered, otherwise throws an exception.
     *
     * @return The registered object
     */
    @Override
    T get();

    /**
     * Gets the object if it has been registered, or creates and returns it if not.
     *
     * @return The object (either registered or newly created)
     */
    T getOrCreate();

    /**
     * Creates a new identifier with the given prefix and suffix.
     *
     * @param pre  The prefix to add
     * @param post The suffix to add
     * @return A new identifier
     */
    Identifier newID(String pre, String post);
}