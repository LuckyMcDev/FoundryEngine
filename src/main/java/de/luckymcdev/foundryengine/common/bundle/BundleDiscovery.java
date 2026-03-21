package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.bundle.toml.BundleTomlParser;
import de.luckymcdev.foundryengine.common.exeptions.EngineException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Handles discovery of bundles from the file system.
 */
public class BundleDiscovery {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final PathMatcher BUNDLES_FILE_MATCH =
            FileSystems.getDefault().getPathMatcher("glob:*.bundles.toml");

    private final BundleFactory bundleFactory;
    private final Consumer<Bundle> bundleConsumer;

    public BundleDiscovery(BundleFactory bundleFactory, Consumer<Bundle> bundleConsumer) {
        this.bundleFactory = bundleFactory;
        this.bundleConsumer = bundleConsumer;
    }

    /**
     * Discovers and loads all bundles in the given directory.
     *
     * @param directory The directory to search
     * @throws IOException if discovery fails
     */
    public void discover(Path directory) throws IOException {
        LOGGER.debug("Discovering Bundles in: {}", directory);
        int loadedCount = 0;

        try (Stream<Path> stream = Files.list(directory)) {
            for (Path path : stream.toList()) {
                try {
                    if (Files.isDirectory(path)) {
                        if (checkAndLoadBundle(path, null)) {
                            loadedCount++;
                        }
                    } else if (path.toString().endsWith(".zip")) {
                        loadedCount += discoverZip(path);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to check bundle: {}", path, e);
                }
            }
        }

        LOGGER.info("Loaded {} Bundles.", loadedCount);
    }

    /**
     * Opens a ZIP file and searches for bundles inside.
     * Ownership of the FileSystem is transferred to any Bundle created.
     */
    private int discoverZip(Path zipPath) throws IOException {
        FileSystem zipFs = FileSystems.newFileSystem(zipPath, (ClassLoader) null);
        try {
            Path root = zipFs.getPath("/");
            return checkAndLoadBundle(root, zipFs) ? 1 : 0;
        } catch (Exception e) {
            // If we fail before creating a Bundle, close the FileSystem
            try {
                zipFs.close();
            } catch (IOException ignore) {
                // ignore, as we failed.
            }
            throw e;
        }
    }

    /**
     * Checks if a directory contains a bundle and loads it if found.
     *
     * @param directory The directory to check
     * @param zipFs     Optional ZIP FileSystem (null for folder bundles)
     * @return true if a bundle was found and loaded
     */
    private boolean checkAndLoadBundle(Path directory, FileSystem zipFs) throws IOException {
        if (!hasBundleToml(directory)) {
            if (zipFs != null) {
                zipFs.close();
            }
            return false;
        }

        LOGGER.debug("Found Bundle directory: {}", directory);
        loadBundlesInDirectory(directory, zipFs);
        return true;
    }

    private boolean hasBundleToml(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files.anyMatch(file -> BUNDLES_FILE_MATCH.matches(file.getFileName()));
        } catch (IOException e) {
            return false;
        }
    }

    private void loadBundlesInDirectory(Path directory, FileSystem zipFs) {
        List<Path> bundleFiles = getBundleFiles(directory);

        if (bundleFiles.size() > 1) {
            throw new EngineException("More than one bundle file exists for bundle: " + directory);
        }

        bundleFiles.forEach(file -> loadBundleFile(file, directory, zipFs));
    }

    private List<Path> getBundleFiles(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(file -> BUNDLES_FILE_MATCH.matches(file.getFileName()))
                    .toList();
        } catch (IOException e) {
            LOGGER.error("Failed to list bundle files in: {}", directory, e);
            return Collections.emptyList();
        }
    }

    private void loadBundleFile(Path file, Path bundleDir, FileSystem zipFs) {
        try {
            String content = Files.readString(file);
            List<BundleInfo> infos = BundleTomlParser.parse(content);

            for (BundleInfo info : infos) {
                loadBundle(info, bundleDir, zipFs);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read bundle file: {}", file, e);
        }
    }

    private void loadBundle(BundleInfo info, Path bundleDir, FileSystem zipFs) {
        try {
            Bundle bundle = bundleFactory.createBundle(info, bundleDir, zipFs);
            bundleConsumer.accept(bundle);
        } catch (IOException e) {
            LOGGER.error("Failed to create bundle '{}': {}", info.id(), e.getMessage(), e);
        }
    }

    public BundleFactory getBundleFactory() {
        return bundleFactory;
    }
}