package io.github.luckymcdev.common.registry;

public interface Registry<K, V> {
    void register(K key, V value);

    V get(K key);

    boolean contains(K key);

    void remove(K key);
}
