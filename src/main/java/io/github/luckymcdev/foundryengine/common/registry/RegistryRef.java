package io.github.luckymcdev.foundryengine.common.registry;

public class RegistryRef<K, V> {
    private final K key;
    private V value;

    public RegistryRef(K key) {
        this.key = key;
    }

    public RegistryRef(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public boolean isSet() {
        return value != null;
    }
}
