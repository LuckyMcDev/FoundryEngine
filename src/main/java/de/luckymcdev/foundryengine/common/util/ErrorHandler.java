package de.luckymcdev.foundryengine.common.util;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Centralized error handler for script errors with server broadcast support.
 */
public final class ErrorHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	private ErrorHandler() {
	}

	/**
	 * Logs and broadcasts a script error to the server's players.
	 */
	public static void handleScriptError(String context, Throwable error) {
		StackTraceElement scriptFrame = findScriptFrame(error);
		String scriptLoc = scriptFrame != null ? " at " + scriptFrame.getFileName() + ":" + scriptFrame.getLineNumber() : "";

		LOGGER.error("Uncaught error in {}{}", context, scriptLoc, error);

		var server = ServerLifecycleHooks.getCurrentServer();
		String loc = scriptFrame != null
			? " (" + scriptFrame.getFileName() + ":" + scriptFrame.getLineNumber() + ")"
			: (error.getStackTrace().length > 0
				? " (" + error.getStackTrace()[0].getFileName() + ":" + error.getStackTrace()[0].getLineNumber() + ")"
				: "");

		String message = "§c[Script Error] " + context + ": " + getShortErrorMessage(error) + loc;

		if (server != null) {
			server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
		} else {
			LOGGER.error("{} (no server): {}{}", context, error.toString(), loc);
		}
	}

	public static String getShortErrorMessage(Throwable error) {
		String msg = error.getMessage();
		if (msg == null || msg.isEmpty()) {
			return error.getClass().getSimpleName();
		}
		// If it's a multi-line message, take the first line
		int newline = msg.indexOf('\n');
		if (newline != -1) {
			msg = msg.substring(0, newline);
		}
		return error.getClass().getSimpleName() + ": " + msg;
	}

	public static String getFormattedMessage(Throwable error) {
		String msg = error.getMessage();
		if (msg == null || msg.isEmpty()) {
			return "An unexpected error occurred";
		}

		// Strip common technical prefixes if they exist
		if (msg.startsWith(error.getClass().getName() + ": ")) {
			msg = msg.substring(error.getClass().getName().length() + 2);
		}

		int newline = msg.indexOf('\n');
		if (newline != -1) {
			msg = msg.substring(0, newline);
		}

		return msg;
	}

	public static @Nullable StackTraceElement findScriptFrame(Throwable error) {
		StackTraceElement[] stackTrace = error.getStackTrace();
		if (stackTrace.length == 0) {
			return null;
		}

		// First pass: find a script frame with a line number
		for (StackTraceElement element : stackTrace) {
			String fileName = element.getFileName();
			if (fileName != null && (fileName.endsWith(".groovy") || fileName.endsWith(".gvy"))) {
				if (element.getLineNumber() > 0) {
					return element;
				}
			}
		}

		// Second pass: find any script frame
		for (StackTraceElement element : stackTrace) {
			String fileName = element.getFileName();
			if (fileName != null && (fileName.endsWith(".groovy") || fileName.endsWith(".gvy"))) {
				return element;
			}
		}

		return null;
	}
}
