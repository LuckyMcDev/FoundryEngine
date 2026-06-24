package de.luckymcdev.foundryengine.common.registry;

import net.minecraft.util.RandomSource;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Generic registry interface for key-value storage with freeze support.
 *
 * @param <K> Key Type.
 * @param <V> Value Type.
 */
public interface Registry<K, V> {
    /**
     * Register a key-value pair in this registry.
     */
    void register(K key, V value);

    /**
     * Remove a key from this registry.
     */
    void remove(K key);

    /**
     * Get the value directly (legacy method, prefer getRef()).
     * Returns null or default value if not found.
     */
    V get(K key);

    /**
     * Get a reference to the registry entry.
     * This is the preferred way to access registry entries.
     *
     * @return RegistryRef that can be cached and provides lazy loading
     */
    RegistryRef<K, V> getRef(K key);

    /**
     * Get the key for a given value (reverse lookup).
     */
    K getKey(V value);

    /**
     * Get a random value from the registry.
     */
    V getRandom(RandomSource random);

    /**
     * Try to get a value, returning Optional.
     */
    Optional<V> tryGet(K key);

    /**
     * Check if the registry contains a key.
     */
    boolean contains(K key);

    /**
     * Get all values in the registry.
     */
    Collection<V> values();

    /**
     * Get all keys in the registry.
     */
    Collection<K> keys();

    /**
     * Check if the registry is frozen (immutable).
     */
    boolean isFrozen();

    /**
     * Freeze the registry, making it immutable.
     */
    void freeze();

    /**
     * Unfreeze the registry, allowing modifications again.
     */
    void unfreeze();

    /**
     * Register a callback to run when the registry is frozen.
     *
     * @return true if callback was added, false if already frozen (callback ran immediately)
     */
    boolean onFreeze(Runnable callback);

    /**
     * Iterate over all key-value pairs.
     */
    void forEach(BiConsumer<K, V> kvConsumer);

    /**
     * Iterate over all values.
     */
    void forEach(Consumer<V> action);

    /**
     * Stream all values.
     */
    Stream<V> stream();

    /**
     * Clear the registry (only if not frozen).
     */
    void clear() throws IllegalStateException;

    /**
     * Get the number of entries in the registry.
     */
    int size();
}