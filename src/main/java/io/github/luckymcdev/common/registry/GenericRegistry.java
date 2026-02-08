package io.github.luckymcdev.common.registry;

import java.util.*;

public class GenericRegistry<K, V> implements Registry<K, V> {
    private final Map<K, V> registryMap = new HashMap<>();

    @Override
    public void register(K key, V value) {
        registryMap.put(key, value);
    }

    @Override
    public V get(K key) {
        return registryMap.get(key);
    }

    @Override
    public boolean contains(K key) {
        return registryMap.containsKey(key);
    }

    public Collection<V> getValues() {
        return registryMap.values();
    }

    @Override
    public void remove(K key) {
        registryMap.remove(key);
    }
}