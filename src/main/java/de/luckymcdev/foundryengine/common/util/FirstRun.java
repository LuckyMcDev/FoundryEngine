package de.luckymcdev.foundryengine.common.util;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple Util to check if it's a mods first load.
 */
public class FirstRun {
	private static final List<String> notFirstLoad = new ArrayList<>();
	private static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * Checks if this is the first run for the given mod since startup.
	 */
	public static boolean isFor(String modTarget) {
		if (ModList.get().isLoaded(modTarget)) {
			if (!notFirstLoad.contains(modTarget)) {
				notFirstLoad.add(modTarget);
				return true;
			} else {
				return false;
			}
		} else {
			LOGGER.warn("First Run query for mod: {} failed because that mod is not loaded.", modTarget);
			return false;
		}
	}
}
