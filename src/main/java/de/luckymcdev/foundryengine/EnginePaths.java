package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.lang3.SystemProperties;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.luckymcdev.foundryengine.Common.MODID;
import static de.luckymcdev.foundryengine.Common.MODNAME;

public interface EnginePaths {
	Logger LOGGER = LogUtils.getLogger();
	Path GAMEDIR = FMLPaths.GAMEDIR.get().normalize().toAbsolutePath();
	Path CONFIG = FMLPaths.CONFIGDIR.get();
	Path TEMP_DIR = Path.of(SystemProperties.getProperty("java.io.tmpdir")).resolve(MODID);
	Path USER_SPACE = Paths.get(System.getProperty("user.home"), "."+MODID);
	Path DIRECTORY = dir(GAMEDIR.resolve(MODNAME));
	Path BUNDLES = dir(DIRECTORY.resolve("bundles"));
	Path CACHE = dir(DIRECTORY.resolve(".cache"));
	Path FIRST_RUN_CACHE = CACHE.resolve("firstruncache.json");
	Path DUMPS = dir(CACHE.resolve("dumps"));
	Path GAME = dir(CACHE.resolve("game"));
	Path ENGINE_DATA = CACHE.resolve("engine.dat");
	Path PAKKU_FILE = dir(CACHE.resolve("pakku")).resolve("pakku.jar");
	boolean IS_PAKKU = PAKKU_FILE.toFile().exists();
	Path LIBRARIES = dir(CACHE.resolve("libraries"));
	Path CONFIG_FE = dir(DIRECTORY.resolve("config"));

	static Path dir(Path path) {
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			LOGGER.error("Failed to create directory: {}", path, e);
		}
		return path;
	}

	static Path file(Path path, String content) {
		if (Files.notExists(path)) {
			try {
				Files.createDirectories(path.getParent());
				Files.writeString(path, content);
			} catch (IOException e) {
				LOGGER.error("Failed to create file: {}", path, e);
			}
		}
		return path;
	}

	static Path file(Path path) {
		return file(path, "");
	}

	/**
	 * Resolves a user‑supplied path to its canonical form and ensures it lies inside
	 * the Minecraft game directory. Symlinks are followed, and non‑existing paths
	 * are safely resolved by canonicalizing their deepest existing ancestor.
	 *
	 * @param path the path to validate and resolve
	 * @return the resolved, absolute, canonical path (if it exists), or a safe reconstruction
	 * @throws IOException       if an I/O error occurs during canonicalization
	 * @throws SecurityException if the resolved path is outside {@link #GAMEDIR}
	 */
	static Path resolveAndValidate(Path path) throws IOException {
		path = path.normalize().toAbsolutePath();
		Path base = GAMEDIR.toRealPath();

		Path resolved;
		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			try {
				resolved = path.toRealPath();
			} catch (NoSuchFileException e) {
				throw new IOException("Broken symbolic link: " + path, e);
			}
		} else {
			Path current = path;
			while (current != null && !Files.exists(current)) {
				current = current.getParent();
			}
			if (current == null) {
				resolved = path;
			} else {
				Path realCurrent = current.toRealPath();
				Path relative = current.relativize(path);
				resolved = realCurrent.resolve(relative);
			}
		}

		if (!resolved.startsWith(base)) {
			throw new SecurityException("Resolved path " + resolved + " is outside the Minecraft directory " + base);
		}
		return resolved;
	}

	/**
	 * Like {@link #resolveAndValidate(Path)}, but the last path component is NOT resolved.
	 * If {@code path} is a symbolic link, the returned path is the link itself rather than
	 * its target. Only the parent directory is canonicalized and checked against
	 * {@link #GAMEDIR}. Use this for operations that act on the link (e.g. deleting it).
	 *
	 * @throws SecurityException if the parent directory is outside {@link #GAMEDIR}
	 */
	static Path resolveAndValidateNoFollow(Path path) throws IOException {
		path = path.normalize().toAbsolutePath();
		Path name = path.getFileName();
		Path parent = path.getParent();
		if (name == null || parent == null) {
			throw new SecurityException("Path has no parent inside the Minecraft directory: " + path);
		}
		return resolveAndValidate(parent).resolve(name.toString());
	}

	/**
	 * Reads the full content of a file as a UTF-8 string. Returns empty on failure.
	 */
	static String getFileContent(@Nullable Path file) {
		if (file == null) {
			return "";
		}
		try (InputStream is = Files.newInputStream(file)) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOGGER.error("Failed to read file: {}", file, e);
			return "";
		}
	}
}
