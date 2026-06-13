package de.luckymcdev.foundryengine.common.util;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

public final class ErrorHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ErrorHandler() {
    }

    public static void handleScriptError(String context, Throwable error) {
        LOGGER.error("Uncaught error in {}", context, error);

        var server = ServerLifecycleHooks.getCurrentServer();
        String loc = error.getStackTrace().length > 0
                ? " (" + error.getStackTrace()[0].getFileName() + ":" + error.getStackTrace()[0].getLineNumber() + ")"
                : "";

        String message = "§c[Script Error] " + context + ": " + error + loc;

        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
        } else {
            LOGGER.error("{} (no server): {}", context, error.toString());
        }
    }
}
