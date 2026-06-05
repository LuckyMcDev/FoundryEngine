package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.exceptions.UtilityClassException;
import de.luckymcdev.foundryengine.server.Server;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import org.slf4j.Logger;

public final class BundleExceptionHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private BundleExceptionHandler() {
        throw new UtilityClassException();
    }

    public static void handle(String context, Exception e) {
        LOGGER.error("{}: {}", context, e.getMessage(), e);
        ModLoadingIssue issue = ModLoadingIssue.error(context + ": " + e.getMessage());
        ModLoader.addLoadingIssue(issue);
        if (Server.getServer() != null) {
            String loc = e.getStackTrace().length > 0
                    ? " (" + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber() + ")"
                    : "";
            Server.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("§c[Script Error] " + context + ": " + e + loc), false);
        }
    }
}
