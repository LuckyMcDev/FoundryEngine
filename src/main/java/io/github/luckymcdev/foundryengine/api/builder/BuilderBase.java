package io.github.luckymcdev.foundryengine.api.builder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

/**
 * Base interface for all builders in the Foundry Engine API.
 * Provides common methods for translation keys, display names, and tags.
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
     * @throws io.github.luckymcdev.foundryengine.common.exeptions.EngineException if the object hasn't been registered
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
     * Sets a custom translation key for this object.
     *
     * @param key The translation key
     * @return This builder for chaining
     */
    BuilderBase<T> getTranslationKey(String key);

    /**
     * Sets the display name component for this object.
     *
     * @param name The display name
     * @return This builder for chaining
     */
    BuilderBase<T> setDisplayName(Component name);

    /**
     * Marks the display name as formatted (preserves formatting).
     *
     * @return This builder for chaining
     */
    BuilderBase<T> formattedDisplayName();

    /**
     * Sets a formatted display name.
     *
     * @param name The formatted display name
     * @return This builder for chaining
     */
    BuilderBase<T> formattedDisplayName(Component name);

    /**
     * Adds default tags to this object.
     *
     * @param tag Array of tag identifiers
     * @return This builder for chaining
     */
    BuilderBase<T> tag(Identifier[] tag);

    /**
     * Gets the translation key for this builder.
     *
     * @return The translation key
     */
    String getBuilderTranslationKey();

    /**
     * Creates a new identifier with the given prefix and suffix.
     *
     * @param pre  The prefix to add
     * @param post The suffix to add
     * @return A new identifier
     */
    Identifier newID(String pre, String post);
}