package de.luckymcdev.foundryengine.common.registry;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A registry interface for ordered lists of values with freeze support.
 */
public interface RegistryList<V> {
    void add(V value);

    void remove(V value);

    void remove(int index);

    V get(int index);

    Collection<V> values();

    boolean contains(V value);

    boolean isFrozen();

    void freeze();

    void unfreeze();

    void forEach(Consumer<? super V> action);

    Stream<V> stream();

    void clear();

    int size();
}
