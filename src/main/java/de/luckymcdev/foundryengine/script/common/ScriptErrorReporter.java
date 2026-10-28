package de.luckymcdev.foundryengine.script.common;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.script.common.runtime.ScriptFailure;
import de.luckymcdev.foundryengine.util.common.ErrorHandler;
import de.luckymcdev.foundryengine.Server;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Logs script failures, collects them for in-game display and broadcasts them to players.
 * Replaces the three copies of this logic that lived in GroovyScriptLoader.
 */
public final class ScriptErrorReporter {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final Map<String, List<Throwable>> collected = new ConcurrentHashMap<>();

	private static String locate(Throwable cause) {
		StackTraceElement frame = ErrorHandler.findScriptFrame(cause);
		if (frame == null && cause.getStackTrace().length > 0) {
			frame = cause.getStackTrace()[0];
		}
		return frame != null ? " (" + frame.getFileName() + ":" + frame.getLineNumber() + ")" : "";
	}

	public void report(String bundleId, ScriptFailure failure) {
		Throwable cause = failure.cause();
		String script = failure.script() != null ? failure.script() : "<bundle>";
		String where = locate(cause);

		LOGGER.warn("Script failure in bundle '{}' ({} of '{}'){}", bundleId, failure.stage(), script, where, cause);
		collected.computeIfAbsent(bundleId, k -> new CopyOnWriteArrayList<>()).add(cause);

		if (Server.getServer() != null) {
			Server.getServer().getPlayerList().broadcastSystemMessage(
				Component.literal("§c[Script Error] " + failure.stage() + " '" + script + "' in bundle '"
					+ bundleId + "': " + ErrorHandler.getShortErrorMessage(cause) + where),
				false);
		}
	}

	/**
	 * Returns and removes everything collected so far, per bundle.
	 */
	public Map<String, List<Throwable>> drain() {
		Map<String, List<Throwable>> result = new HashMap<>();
		for (String bundleId : List.copyOf(collected.keySet())) {
			List<Throwable> errors = collected.remove(bundleId);
			if (errors != null) {
				result.put(bundleId, new ArrayList<>(errors));
			}
		}
		return result;
	}
}