package io.github.luckymcdev.foundryengine.common.registry;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A Reference to an Entry in a {@link Registry} / {@link GenericRegistry}.
 *
 * @param <K> Key Type.
 * @param <V> Value Type.
 */
public class RegistryRef<K, V> {
    private final K key;
    private final Registry<K, V> registry;
    private V cachedValue;

    public RegistryRef(K key, Registry<K, V> registry) {
        this.key = Objects.requireNonNull(key, "Registry key cannot be null");
        this.registry = Objects.requireNonNull(registry, "Registry reference cannot be null");
    }

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

    public K getKey() {
        return key;
    }

    public boolean exists() {
        return registry.contains(key);
    }

    public void ifPresent(Consumer<V> action) {
        V val = get();
        if (val != null) {
            action.accept(val);
        }
    }

    @Override
    public String toString() {
        return "RegistryRef{" + "key=" + key + ", cached=" + (cachedValue != null) + '}';
    }
}