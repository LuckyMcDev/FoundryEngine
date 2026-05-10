package de.luckymcdev.foundryengine.common.world.level;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface RemoveFromRegistry<T> {
    @SuppressWarnings("unchecked")
    static <T> boolean remove(MappedRegistry<T> registry, Identifier key) {
        return ((RemoveFromRegistry<T>) registry).engine$remove(key);
    }

    @SuppressWarnings("unchecked")
    static <T> boolean remove(MappedRegistry<T> registry, T value) {
        return ((RemoveFromRegistry<T>) registry).engine$remove(value);
    }

    @SuppressWarnings("unchecked")
    static <T> RegistryRemoval thaw(Registry<T> registry) {
        RemoveFromRegistry<T> registry1 = ((RemoveFromRegistry<T>) registry);
        boolean priorStateOfMatter = registry1.engine$isFrozen();
        registry1.engine$setFrozen(false);
        return () -> registry1.engine$setFrozen(priorStateOfMatter);
    }

    boolean engine$remove(T value);

    boolean engine$remove(Identifier key);

    void engine$setFrozen(boolean value);

    boolean engine$isFrozen();

    @ApiStatus.NonExtendable
    @FunctionalInterface
    interface RegistryRemoval extends AutoCloseable {
        @Override
        void close();
    }
}
