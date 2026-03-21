package de.luckymcdev.foundryengine.common.util;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class DebugUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void pauseGame() {
        if (isFrozen()) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(() -> {
                LOGGER.info("Game Frozen");
                server.tickRateManager().setFrozen(true);
            });
        }
    }

    public static boolean isFrozen() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        if (server.isSameThread()) {
            return server.tickRateManager().isFrozen();
        }

        return CompletableFuture.supplyAsync(() -> server.tickRateManager().isFrozen(), server).join();
    }

    public static void resumeGame() {
        if (!isFrozen()) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(() -> {
                LOGGER.info("Game Released");
                server.tickRateManager().setFrozen(false);
            });
        }
    }
}
