package de.luckymcdev.foundryengine.interfaces.level;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.nio.file.Path;
import java.util.List;

/**
 * Manages additional storage paths, world path resolution, and instanced world management.
 */
public interface EngineLevelStorageSource extends EngineInterface<LevelStorageSource> {
	/**
	 * Adds an additional path for level storage.
	 */
	default void engine$addAdditionalPath(Path path) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns the list of additional storage paths.
	 */
	default List<Path> engine$getAdditionalPaths() {
		throw new NoMixinException(this);
	}

	/**
	 * Removes an additional storage path, returning true if successful.
	 */
	default boolean engine$removeAdditionalPath(Path path) {
		throw new NoMixinException(this);
	}

	/**
	 * Clears all additional storage paths.
	 */
	default void engine$clearAdditionalPaths() {
		throw new NoMixinException(this);
	}

	/**
	 * Resolves the full path for a given level identifier.
	 */
	default Path engine$resolveWorldPath(String levelId) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns whether the given level ID refers to an external world.
	 */
	default boolean engine$isWorldExternal(String levelId) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns whether the given level ID is an instanced world.
	 */
	default boolean engine$isInstanced(String levelId) {
		throw new NoMixinException(this);
	}

	/**
	 * Deletes the instanced world data for the given level ID.
	 */
	default void engine$deleteInstance(String levelId) {
		throw new NoMixinException(this);
	}

	/**
	 * Clears all instanced world data.
	 */
	default void engine$clearInstanced() {
		throw new NoMixinException(this);
	}
}
