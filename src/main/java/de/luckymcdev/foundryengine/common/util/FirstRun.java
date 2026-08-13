package de.luckymcdev.foundryengine.common.util;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static de.luckymcdev.foundryengine.common.Common.FIRST_RUN_CACHE;

/**
 * Simple Util to check if it's a mods first load.
 */
public class FirstRun {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Set<String> seenMods = new HashSet<>();

	static {
		loadCache();
	}

	private static void loadCache() {
		if (Files.exists(FIRST_RUN_CACHE)) {
			try {
				Files.readAllLines(FIRST_RUN_CACHE).stream()
					.map(String::trim)
					.filter(line -> !line.isEmpty())
					.forEach(seenMods::add);
				LOGGER.debug("Loaded {} first-run entries from cache.", seenMods.size());
			} catch (IOException e) {
				LOGGER.error("Failed to read first-run cache file: {}", FIRST_RUN_CACHE, e);
			}
		} else {
			LOGGER.debug("No first-run cache file found; will create on first write.");
		}
	}

	private static void saveCache() {
		try {
			Files.createDirectories(FIRST_RUN_CACHE.getParent());
			Files.write(FIRST_RUN_CACHE, seenMods);
		} catch (IOException e) {
			LOGGER.error("Failed to write first-run cache file: {}", FIRST_RUN_CACHE, e);
		}
	}

	public static boolean isFor(String modTarget) {
		if (!ModList.get().isLoaded(modTarget)) {
			LOGGER.warn("First Run query for mod: {} failed because that mod is not loaded.", modTarget);
			return false;
		}

		if (seenMods.contains(modTarget)) {
			return false;
		}

		seenMods.add(modTarget);
		saveCache();
		return true;
	}

	public static void resetCache() {
		seenMods.clear();
		try {
			Files.deleteIfExists(FIRST_RUN_CACHE);
		} catch (IOException e) {
			LOGGER.error("Failed to delete cache file: {}", FIRST_RUN_CACHE, e);
		}
	}
}