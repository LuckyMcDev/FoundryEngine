package io.github.luckymcdev.foundryengine.common.registry;

import net.minecraft.util.RandomSource;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * {@link GenericRegistry}
 *
 * @param <K> Key Type.
 * @param <V> Value Type.
 */
public interface Registry<K, V> {
    void register(K key, V value);

    void remove(K key);

    V get(K key);

    K getKey(V value);

    V getRandom(RandomSource random);

    Optional<V> tryGet(K key);

    boolean contains(K key);

    Collection<V> values();

    Collection<K> keys();

    boolean isFrozen();

    void freeze();

    void unfreeze();

    boolean onFreeze(Runnable callback);

    void forEach(BiConsumer<K, V> kvConsumer);

    void forEach(Consumer<V> action);

    Stream<V> stream();

    void clear() throws IllegalStateException;

    int size();
}
