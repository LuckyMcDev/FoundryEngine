package io.github.luckymcdev.foundryengine.client.ext;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

public class ModPathBroadcaster {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String GRADLE_HOST = "localhost";
    private static final int GRADLE_PORT = 56656;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    private static final String THREAD_NAME = "ModPathBroadcaster-Thread";

    public static void onClientSetup() {
        Path modsPath = FMLPaths.MODSDIR.get();
        broadcastModPath(modsPath.toAbsolutePath().toString());
    }

    private static void broadcastModPath(String modPath) {
        Thread.ofVirtual().name(THREAD_NAME).start(() -> {
            LOGGER.debug("Starting mod path broadcast for: {}", modPath);

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                    LOGGER.debug("Attempt {}/{} - Connecting to {}:{}", attempt, MAX_RETRIES, GRADLE_HOST, GRADLE_PORT);

                    try (Socket socket = new Socket(GRADLE_HOST, GRADLE_PORT)) {
                        socket.setSoTimeout(CONNECTION_TIMEOUT_MS);

                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(modPath);

                        if (socket.getInputStream().read() != -1) {
                            LOGGER.debug("Successfully sent mod path to Gradle");
                            return;
                        }
                        LOGGER.error("No acknowledgment received from Gradle");
                    }
                } catch (IOException e) {
                    LOGGER.error("Connection failed on attempt {}: {}", attempt, e.getMessage());
                } catch (InterruptedException e) {
                    LOGGER.error("Broadcast thread interrupted");
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            LOGGER.error("Failed to broadcast mod path after {} attempts", MAX_RETRIES);
        });
    }
}