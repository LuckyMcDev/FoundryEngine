package de.luckymcdev.foundryengine.common.world;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.config.CommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource.LevelDirectory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Manages additional world storage directories and world instancing for pack distribution.
 *
 * <p>All state lives in process-global static fields. This is only safe for a
 * single-player / single-client process; it is not designed for multi-process or
 * multi-instance server hosting where isolated per-instance state would be required.</p>
 */
public class StorageSourceManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final List<Path> additionalBaseDirs = new ArrayList<>();
	private static final Map<String, Path> instancedWorlds = new HashMap<>();
	private static final Object LOCK = new Object();

	/**
	 * Returns whether the given level id is safe to use as a single path name.
	 * Rejects blank ids, absolute paths, path-separator navigation, and "." / ".." segments.
	 */
	private static boolean isValidLevelId(String levelId) {
		if (levelId == null || levelId.isBlank()) {
			return false;
		}
		if (levelId.indexOf('/') != -1 || levelId.indexOf('\\') != -1) {
			return false;
		}
		Path name = Path.of(levelId);
		if (name.isAbsolute() || name.getNameCount() != 1) {
			return false;
		}
		String segment = name.getName(0).toString();
		return !segment.equals(".") && !segment.equals("..");
	}

	/**
	 * Throws if the given level id is not safe to use as a single path name.
	 */
	private static void requireValidLevelId(String levelId) {
		if (!isValidLevelId(levelId)) {
			throw new IllegalArgumentException("Invalid level id: '" + levelId + "'");
		}
	}

	/**
	 * Adds an additional directory to search for world data.
	 */
	public static void addAdditionalPath(Path path) {
		if (path != null && Files.isDirectory(path)) {
			Path normalized = path.toAbsolutePath().normalize();
			synchronized (LOCK) {
				if (!additionalBaseDirs.contains(normalized)) {
					additionalBaseDirs.add(normalized);
					LOGGER.info("Added extra world directory: {}", normalized);
				}
			}
		} else {
			LOGGER.warn("Attempted to add invalid or non-existent path: {}", path);
		}
	}

	/**
	 * Returns an unmodifiable list of additional world directories.
	 */
	public static List<Path> getAdditionalPaths() {
		synchronized (LOCK) {
			return Collections.unmodifiableList(additionalBaseDirs);
		}
	}

	/**
	 * Removes an additional world directory by path.
	 */
	public static boolean removeAdditionalPath(Path path) {
		if (path == null) {
			return false;
		}
		Path normalized = path.toAbsolutePath().normalize();
		boolean removed;
		synchronized (LOCK) {
			removed = additionalBaseDirs.remove(normalized);
		}
		if (removed) {
			LOGGER.info("Removed extra world directory: {}", normalized);
		}
		return removed;
	}

	/**
	 * Clears all additional world directories.
	 */
	public static void clearAdditionalPaths() {
		synchronized (LOCK) {
			additionalBaseDirs.clear();
		}
		LOGGER.info("Cleared all extra world directories");
	}

	/**
	 * Resolves the path for a world, checking additional directories first.
	 */
	public static Path resolveWorldPath(Path baseDir, String levelId) {
		requireValidLevelId(levelId);
		Path primary = baseDir.resolve(levelId);
		Path normalizedBase = baseDir.toAbsolutePath().normalize();
		Path normalizedPrimary = primary.toAbsolutePath().normalize();
		if (!normalizedPrimary.startsWith(normalizedBase)) {
			LOGGER.warn("Resolved world path {} escapes base directory {}; refusing", normalizedPrimary, normalizedBase);
			throw new IllegalArgumentException("Resolved world path escapes base directory");
		}
		if (Files.isDirectory(primary)) {
			return primary;
		}
		synchronized (LOCK) {
			for (Path dir : additionalBaseDirs) {
				Path candidate = dir.resolve(levelId);
				if (Files.isDirectory(candidate)) {
					return candidate;
				}
			}
		}
		return primary;
	}

	/**
	 * Checks if a world is stored in an additional directory rather than the primary.
	 */
	public static boolean isWorldExternal(Path baseDir, String levelId) {
		if (!isValidLevelId(levelId)) {
			LOGGER.warn("Ignoring invalid level id '{}' for external world check", levelId);
			return false;
		}
		Path primary = baseDir.resolve(levelId);
		if (Files.isDirectory(primary)) {
			return false;
		}
		synchronized (LOCK) {
			for (Path dir : additionalBaseDirs) {
				if (Files.isDirectory(dir.resolve(levelId))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Adds world candidates from all additional directories to the output list.
	 */
	public static void addCandidatesFromAdditionalDirs(List<LevelDirectory> out) throws LevelStorageException {
		synchronized (LOCK) {
			for (Path dir : additionalBaseDirs) {
				addCandidatesFromDir(dir, out);
			}
		}
	}

	private static void addCandidatesFromDir(Path dir, List<LevelDirectory> out) throws LevelStorageException {
		if (!Files.isDirectory(dir)) {
			return;
		}
		try (Stream<Path> streams = Files.list(dir)) {
			streams.filter(Files::isDirectory)
				.map(LevelDirectory::new)
				.filter(d -> Files.isRegularFile(d.dataFile()) || Files.isRegularFile(d.oldDataFile()))
				.forEach(out::add);
		} catch (IOException e) {
			throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
		}
	}

	/**
	 * Checks if a world level has been instanced.
	 */
	public static boolean isInstanced(String levelId) {
		synchronized (LOCK) {
			return instancedWorlds.containsKey(levelId);
		}
	}

	/**
	 * Returns the path to an instanced copy of the given world.
	 */
	public static Path getInstancedPath(String levelId) {
		synchronized (LOCK) {
			return instancedWorlds.get(levelId);
		}
	}

	/**
	 * Deletes the instanced copy of the given world.
	 */
	public static void deleteInstance(String levelId) {
		if (!isValidLevelId(levelId)) {
			LOGGER.warn("Ignoring invalid level id '{}' for instance deletion", levelId);
			return;
		}
		Path target;
		synchronized (LOCK) {
			target = instancedWorlds.remove(levelId);
		}
		if (target != null && Files.isDirectory(target)) {
			try {
				FileUtils.deleteDirectory(target.toFile());
				LOGGER.info("Deleted instanced copy of world '{}'", levelId);
			} catch (IOException e) {
				LOGGER.error("Failed to delete instanced copy of world '{}'", levelId, e);
			}
		}
	}

	/**
	 * Clears all instanced worlds and deletes the instances directory.
	 */
	public static void clearInstanced() {
		synchronized (LOCK) {
			instancedWorlds.clear();
		}
		try {
			Path instancesDir = Common.TEMP_DIR.resolve("instances").resolve("worlds");
			if (Files.isDirectory(instancesDir)) {
				FileUtils.deleteDirectory(instancesDir.toFile());
			}
		} catch (IOException e) {
			LOGGER.error("Failed to delete instanced worlds directory", e);
		}
	}

	/**
	 * Ensures an instanced copy of the world exists, copying from source if needed.
	 */
	public static void ensureInstanced(String levelId, Path source) throws IOException {
		requireValidLevelId(levelId);
		String packMode = CommonConfig.PACK_MODE.get();
		if (packMode.equalsIgnoreCase("dev")) {
			LOGGER.info("Pack mode '{}': loading world '{}' directly (no instancing)", packMode, levelId);
			return;
		}
		synchronized (LOCK) {
			if (instancedWorlds.containsKey(levelId)) {
				return;
			}
		}

		Path target = getInstanceDir(levelId, source);

		if (Files.exists(target)) {
			try {
				long sourceTime = Files.getLastModifiedTime(source).toMillis();
				long targetTime = Files.getLastModifiedTime(target).toMillis();
				if (sourceTime <= targetTime) {
					synchronized (LOCK) {
						instancedWorlds.put(levelId, target);
					}
					return;
				}
			} catch (IOException ignored) {
			}
			LOGGER.info("Original newer, re-instancing world '{}'", levelId);
		}

		LOGGER.info("Instancing world '{}' from {} to {}", levelId, source, target);
		Files.createDirectories(target);
		try (var files = Files.walk(source)) {
			for (Path file : (Iterable<Path>) files::iterator) {
				Path relative = source.relativize(file);
				Path dest = target.resolve(relative);
				if (Files.isDirectory(file)) {
					Files.createDirectories(dest);
				} else {
					Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}

		synchronized (LOCK) {
			instancedWorlds.put(levelId, target);
		}
	}

	/**
	 * Returns the target directory for an instanced copy of the given world.
	 */
	public static Path getInstanceDir(String levelId, Path source) {
		requireValidLevelId(levelId);
		String key;
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
				.digest(source.toAbsolutePath().normalize().toString().getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 8; i++) {
				sb.append(String.format(Locale.ROOT, "%02x", digest[i]));
			}
			key = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			key = Integer.toHexString(source.hashCode());
		}
		return Common.TEMP_DIR.resolve("instances").resolve("worlds").resolve(key).resolve(levelId);
	}
}
