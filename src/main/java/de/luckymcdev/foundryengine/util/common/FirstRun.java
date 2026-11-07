package de.luckymcdev.foundryengine.util.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.EnginePaths;
import de.luckymcdev.foundryengine.exception.common.UtilityClassException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Simple Util to check if it's a mods first load.
 */
public final class FirstRun {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static JsonObject content = new JsonObject();
	private static boolean loaded = false;

	private FirstRun() {
		throw new UtilityClassException();
	}

	private static void ensureLoaded() {
		if (loaded) {
			return;
		}
		loaded = true;
		if (!Files.exists(EnginePaths.FIRST_RUN_CACHE)) {
			return;
		}
		try {
			JsonObject parsed = GSON.fromJson(Files.readString(EnginePaths.FIRST_RUN_CACHE), JsonObject.class);
			if (parsed != null) {
				content = parsed;
			}
		} catch (IOException | JsonParseException e) {
			LOGGER.error("Failed to load first run cache, treating as empty", e);
		}
	}

	/**
	 * Returns true the first time a mod is seen; records the mod as having run.
	 */
	public static synchronized boolean isFirstRun(String modid) {
		ensureLoaded();
		if (content.has(modid)) {
			return false;
		}
		content.addProperty(modid, true);
		saveCache();
		return true;
	}

	/**
	 * Returns true if the mod has not been recorded yet, without recording it.
	 */
	public static synchronized boolean peek(String modid) {
		ensureLoaded();
		return !content.has(modid);
	}

	public static synchronized void resetCache() {
		content = new JsonObject();
		loaded = true;
		saveCache();
	}

	private static void saveCache() {
		try {
			Files.createDirectories(EnginePaths.FIRST_RUN_CACHE.getParent());
			Files.writeString(EnginePaths.FIRST_RUN_CACHE, GSON.toJson(content));
		} catch (IOException e) {
			LOGGER.error("Failed to save first run cache", e);
		}
	}
}