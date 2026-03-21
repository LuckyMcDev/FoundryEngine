package de.luckymcdev.foundryengine.common.registry;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Reference to an Entry in a {@link Registry} / {@link GenericRegistry}.
 *
 * @param <K> Key Type.
 * @param <V> Value Type.
 */
public class RegistryRef<K, V> {
    private final K key;
    private final Registry<K, V> registry;
    private volatile V cachedValue;

    public RegistryRef(K key, Registry<K, V> registry) {
        this.key = Objects.requireNonNull(key, "Registry key cannot be null");
        this.registry = Objects.requireNonNull(registry, "Registry reference cannot be null");
    }

    /**
     * Get the value from the registry.
     * Caches the value if the registry is frozen.
     *
     * @return the value, or null if not registered
     */
    public V get() {
        if (cachedValue != null) {
            return cachedValue;
        }

        V value = registry.get(key);

        if (registry.isFrozen() && value != null) {
            this.cachedValue = value;
        }

        return value;
    }

    /**
     * Get the value or a default if not present.
     *
     * @param defaultValue value to return if entry doesn't exist
     * @return the value or default
     */
    public V orElse(V defaultValue) {
        V value = get();
        return value != null ? value : defaultValue;
    }

    /**
     * Get the value or compute a default if not present.
     *
     * @param supplier supplier for default value
     * @return the value or computed default
     */
    public V orElseGet(Supplier<? extends V> supplier) {
        V value = get();
        return value != null ? value : supplier.get();
    }

    /**
     * Get the value as an Optional.
     *
     * @return Optional containing the value, or empty if not registered
     */
    public Optional<V> toOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * Get the key this reference points to.
     * @return the registry key
     */
    public K getKey() {
        return key;
    }

    /**
     * Check if this entry exists in the registry.
     * @return true if the key is registered
     */
    public boolean exists() {
        return registry.contains(key);
    }

    /**
     * Check if the value is cached (registry is frozen).
     *
     * @return true if value is cached
     */
    public boolean isCached() {
        return cachedValue != null;
    }

    /**
     * Execute an action if the value exists.
     * @param action action to execute with the value
     */
    public void ifPresent(Consumer<V> action) {
        V val = get();
        if (val != null) {
            action.accept(val);
        }
    }

    /**
     * Execute an action if present, or a fallback if not.
     *
     * @param action      action to execute with the value
     * @param emptyAction action to execute if value doesn't exist
     */
    public void ifPresentOrElse(Consumer<V> action, Runnable emptyAction) {
        V val = get();
        if (val != null) {
            action.accept(val);
        } else {
            emptyAction.run();
        }
    }

    /**
     * Map the value to another type.
     *
     * @param mapper function to transform the value
     * @return Optional of the mapped value
     */
    public <U> Optional<U> map(Function<? super V, ? extends U> mapper) {
        V value = get();
        return value != null ? Optional.ofNullable(mapper.apply(value)) : Optional.empty();
    }

    /**
     * Invalidate the cache, forcing next get() to reload from registry.
     * Useful for hot-reloading during development.
     */
    public void invalidateCache() {
        this.cachedValue = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RegistryRef<?, ?> other)) return false;
        return key.equals(other.key) && registry == other.registry;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, System.identityHashCode(registry));
    }

    @Override
    public String toString() {
        return "RegistryRef{" +
                "key=" + key +
                ", cached=" + (cachedValue != null) +
                ", exists=" + exists() +
                '}';
    }
}