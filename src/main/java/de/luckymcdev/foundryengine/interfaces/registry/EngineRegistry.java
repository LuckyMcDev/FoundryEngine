package de.luckymcdev.foundryengine.interfaces.registry;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows removing entries from a {@link MappedRegistry} and toggling its frozen state at runtime.
 */
public interface EngineRegistry<T> extends EngineInterface<MappedRegistry<T>> {
	@SuppressWarnings("unchecked")
	static <T> boolean remove(MappedRegistry<T> registry, Identifier key) {
		return ((EngineRegistry<T>) registry).engine$remove(key);
	}

	@SuppressWarnings("unchecked")
	static <T> boolean remove(MappedRegistry<T> registry, T value) {
		return ((EngineRegistry<T>) registry).engine$remove(value);
	}

	@SuppressWarnings("unchecked")
	static <T> RegistryRemoval thaw(Registry<T> registry) {
		EngineRegistry<T> engineRegistry = ((EngineRegistry<T>) registry);
		boolean wasFrozen = engineRegistry.engine$isFrozen();
		engineRegistry.engine$setFrozen(false);
		return () -> engineRegistry.engine$setFrozen(wasFrozen);
	}

	/**
	 * Removes a registry entry by value.
	 */
	default boolean engine$remove(T value) {
		throw new NoMixinException(this);
	}

	/**
	 * Removes a registry entry by identifier key.
	 */
	default boolean engine$remove(Identifier key) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets the frozen state of the registry.
	 */
	default void engine$setFrozen(boolean value) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns whether the registry is frozen.
	 */
	default boolean engine$isFrozen() {
		throw new NoMixinException(this);
	}

	@ApiStatus.NonExtendable
	@FunctionalInterface
	interface RegistryRemoval extends AutoCloseable {
		@Override
		void close();
	}
}
