package io.github.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class for building BundleFiles from a root directory.
 */
public class BundleFilesBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static BundleFiles build(Path root) {
        Path assets = root.resolve("assets");
        Path data = root.resolve("data");
        Path generated = root.resolve("generated");
        List<Path> scripts = findScripts(root, assets, data);

        return new BundleFiles(root, assets, generated, data, scripts);
    }

    private static List<Path> findScripts(final Path root, final Path assets, final Path data) {
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".groovy"))
                    .filter(f -> !f.startsWith(assets) && !f.startsWith(data))
                    .toList();
        } catch (IOException e) {
            LOGGER.error("Failed to find scripts in: {}", root, e);
            return Collections.emptyList();
        }
    }
}