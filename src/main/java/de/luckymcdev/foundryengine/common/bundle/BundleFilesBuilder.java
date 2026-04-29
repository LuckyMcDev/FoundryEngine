package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class for building BundleFiles from a root directory.
 */
public class BundleFilesBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();

    public BundleFiles build(Path root, @Nullable FileSystem zipFs) {
        Path assets = root.resolve("assets");
        Path data = root.resolve("data");

        Path scriptsRoot = root.resolve("scripts");
        Path clientScripts = scriptsRoot.resolve("client");
        Path commonScripts = scriptsRoot.resolve("common");
        Path serverScripts = scriptsRoot.resolve("server");

        List<Path> scriptCollection = new ArrayList<>();
        scriptCollection.addAll(findScripts(clientScripts));
        scriptCollection.addAll(findScripts(commonScripts));
        scriptCollection.addAll(findScripts(serverScripts));

        BundleFiles.ScriptFiles scriptFiles = new BundleFiles.ScriptFiles(
                scriptsRoot,
                clientScripts,
                commonScripts,
                serverScripts,
                List.copyOf(scriptCollection)
        );

        Path blueprints = root.resolve("blueprints");

        return new BundleFiles(root, assets, data, scriptFiles, blueprints, zipFs);
    }

    private List<Path> findScripts(final Path directory) {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return Collections.emptyList();
        }

        try (Stream<Path> files = Files.walk(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(f -> !Files.isDirectory(f))
                    .toList();
        } catch (IOException e) {
            LOGGER.error("Failed to find scripts in: {}", directory, e);
            return Collections.emptyList();
        }
    }
}