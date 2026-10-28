package de.luckymcdev.foundryengine.ext.client;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.Common;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModPathRecorder {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path RECORD_FILE = Common.USER_SPACE.resolve("last-mods-path.properties");

	public static void record() {
		Path modsPath = FMLPaths.MODSDIR.get().toAbsolutePath();

		try {
			Files.createDirectories(RECORD_FILE.getParent());

			try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(RECORD_FILE))) {
				out.println("modsPath=" + modsPath.toString().replace("\\", "/"));
			}

			LOGGER.debug("Recorded mods path: {}", modsPath);
		} catch (IOException e) {
			LOGGER.error("Failed to record mods path", e);
		}
	}
}