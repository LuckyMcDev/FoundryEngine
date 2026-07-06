package de.luckymcdev.foundryengine.interfaces;

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
	void engine$addAdditionalPath(Path path);

	/**
	 * Returns the list of additional storage paths.
	 */
	List<Path> engine$getAdditionalPaths();

	/**
	 * Removes an additional storage path, returning true if successful.
	 */
	boolean engine$removeAdditionalPath(Path path);

	/**
	 * Clears all additional storage paths.
	 */
	void engine$clearAdditionalPaths();

	/**
	 * Resolves the full path for a given level identifier.
	 */
	Path engine$resolveWorldPath(String levelId);

	/**
	 * Returns whether the given level ID refers to an external world.
	 */
	boolean engine$isWorldExternal(String levelId);

	/**
	 * Returns whether the given level ID is an instanced world.
	 */
	boolean engine$isInstanced(String levelId);

	/**
	 * Deletes the instanced world data for the given level ID.
	 */
	void engine$deleteInstance(String levelId);

	/**
	 * Clears all instanced world data.
	 */
	void engine$clearInstanced();
}
