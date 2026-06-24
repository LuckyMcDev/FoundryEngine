package de.luckymcdev.foundryengine.common.registry;

import net.minecraft.util.RandomSource;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A Registry for registering things.
 *
 * @param <K> Key Type.
 * @param <V> Value Type.
 */
public class GenericRegistry<K, V> implements Registry<K, V> {
    private final ConcurrentHashMap<K, V> primaryLookup = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<V, K> reverseLookup = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<K, RegistryRef<K, V>> refCache = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<K> keys = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<V> values = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> onFreezeCallbacks = new CopyOnWriteArrayList<>();

    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean frozen = false;
    private volatile V defaultValue;

    @Override
    public void register(K key, V value) {
        if (frozen) throw new IllegalStateException("Registry is frozen and cannot be modified.");

        lock.lock();
        try {
            primaryLookup.put(key, value);
            reverseLookup.put(value, key);
            if (!keys.contains(key)) keys.add(key);
            if (!values.contains(value)) values.add(value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        if (frozen) throw new IllegalStateException("Registry is frozen and cannot be modified.");

        lock.lock();
        try {
            V value = primaryLookup.remove(key);
            if (value != null) {
                reverseLookup.remove(value);
                keys.remove(key);
                values.remove(value);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V get(K key) {
        V value = primaryLookup.get(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public RegistryRef<K, V> getRef(K key) {
        return refCache.computeIfAbsent(key, k -> new RegistryRef<>(k, this));
    }

    @Override
    public K getKey(V value) {
        return reverseLookup.get(value);
    }

    @Override
    public V getRandom(RandomSource random) {
        if (values.isEmpty()) return defaultValue;
        return values.get(random.nextInt(values.size()));
    }

    @Override
    public Optional<V> tryGet(K key) {
        return Optional.ofNullable(primaryLookup.get(key));
    }

    @Override
    public boolean contains(K key) {
        return primaryLookup.containsKey(key);
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public Collection<K> keys() {
        return Collections.unmodifiableList(keys);
    }

    @Override
    public boolean isFrozen() {
        return frozen;
    }

    @Override
    public void freeze() {
        if (frozen) return;

        lock.lock();
        try {
            frozen = true;
            onFreezeCallbacks.forEach(Runnable::run);
            onFreezeCallbacks.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unfreeze() {
        lock.lock();
        try {
            frozen = false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean onFreeze(Runnable callback) {
        if (frozen) {
            callback.run();
            return false;
        }
        return onFreezeCallbacks.add(callback);
    }

    @Override
    public void forEach(BiConsumer<K, V> kvConsumer) {
        primaryLookup.forEach(kvConsumer);
    }

    @Override
    public void forEach(Consumer<V> action) {
        values.forEach(action);
    }

    @Override
    public Stream<V> stream() {
        return values.stream();
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            primaryLookup.clear();
            reverseLookup.clear();
            refCache.clear();
            keys.clear();
            values.clear();
            onFreezeCallbacks.clear();
            defaultValue = null;
            frozen = false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        return primaryLookup.size();
    }

    /**
     * Sets the default value returned when a key is not found.
     */
    public void setDefaultValue(V defaultValue) {
        this.defaultValue = defaultValue;
    }
}