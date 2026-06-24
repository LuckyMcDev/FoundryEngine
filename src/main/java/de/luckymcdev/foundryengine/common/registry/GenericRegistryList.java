package de.luckymcdev.foundryengine.common.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Thread-safe implementation of {@link RegistryList} backed by CopyOnWriteArrayList.
 */
public class GenericRegistryList<V> implements RegistryList<V>, Iterable<V> {
    private final CopyOnWriteArrayList<V> values = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> onFreezeCallbacks = new CopyOnWriteArrayList<>();

    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean frozen = false;

    @Override
    public void add(V value) {
        if (frozen) throw new IllegalStateException("Registry is frozen and cannot be modified.");
        values.add(value);
    }

    @Override
    public void remove(V value) {
        if (frozen) throw new IllegalStateException("Registry is frozen and cannot be modified.");
        values.remove(value);
    }

    @Override
    public void remove(int index) {
        if (frozen) throw new IllegalStateException("Registry is frozen and cannot be modified.");
        values.remove(index);
    }

    @Override
    public V get(int index) {
        return values.get(index);
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public boolean contains(V value) {
        return values.contains(value);
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
    public void forEach(Consumer<? super V> action) {
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
            values.clear();
            onFreezeCallbacks.clear();
            frozen = false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public Iterator<V> iterator() {
        return values.iterator();
    }
}
