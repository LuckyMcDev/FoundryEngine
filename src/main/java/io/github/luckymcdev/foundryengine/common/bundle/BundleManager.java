package io.github.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.Commons;
import io.github.luckymcdev.foundryengine.common.Instances;
import io.github.luckymcdev.foundryengine.common.bundle.info.Bundle;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import io.github.luckymcdev.foundryengine.common.bundle.toml.BundleTomlParser;
import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import io.github.luckymcdev.foundryengine.common.script.BundleScriptLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class BundleManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PathMatcher BUNDLES_FILE_MATCH = FileSystems.getDefault().getPathMatcher("glob:*.bundles.toml");
    private final GenericRegistry<String, Bundle> BUNDLES = new GenericRegistry<>();
    private int loadedBundles = 0;

    public BundleManager() {
        Commons.requireInternalAccess(this.getClass());
    }

    public void register(Bundle bundle) {
        BUNDLES.register(bundle.info().getId(), bundle);
        LOGGER.debug("Registered Bundle: {} with Info: {}", bundle.info().getId(), bundle.info());
    }

    public void remove(Bundle bundle) {
        BUNDLES.remove(bundle.info().getId());
    }

    public void discover(Path in) throws IOException {
        LOGGER.debug("Discovering Bundles in: {}", in);
        try (Stream<Path> dirs = Files.list(in)) {
            dirs.filter(Files::isDirectory).forEach(dir -> {
                try {
                    checkBundle(dir);
                } catch (IOException e) {
                    LOGGER.error("Failed to check bundle directory: {}", dir, e);
                }
            });
            LOGGER.info("Loaded {} Bundles.", loadedBundles);
        }
    }

    public void checkBundle(Path in) throws IOException {
        if (!hasBundleToml(in)) return;
        LOGGER.debug("Found Bundle directory: {}", in);
        loadedBundles++;
        discoverInDirectory(in);
    }

    private boolean hasBundleToml(Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.anyMatch(file -> BUNDLES_FILE_MATCH.matches(file.getFileName()));
        } catch (IOException e) {
            return false;
        }
    }

    private void discoverInDirectory(Path dir) {
        List<Path> bundleFiles = getBundleFiles(dir);
        if (bundleFiles.size() > 1) {
            throw new RuntimeException("More than one bundle file exists for bundle: " + dir);
        }
        bundleFiles.forEach(file -> loadBundleFile(file, dir));
    }

    private List<Path> getBundleFiles(Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(file -> BUNDLES_FILE_MATCH.matches(file.getFileName()))
                    .toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private void loadBundleFile(Path file, Path bundleDir) {
        try {
            String content = Files.readString(file);
            BundleTomlParser.parse(content).forEach(info -> loadBundleInfo(info, bundleDir));
        } catch (IOException e) {
            LOGGER.error("Failed to read bundle file: {}", file, e);
        }
    }

    private void loadBundleInfo(BundleInfo info, Path bundleDir) {
        try {
            Bundle bundle = new Bundle(
                    info,
                    buildFileInfo(bundleDir),
                    Instances.getScriptEngineFactory().createScriptEngine(bundleDir),
                    Instances.getEventBusFactory().getEventBusFor(bundleDir));
            register(bundle);
            BundleScriptLoader.loadScripts(bundle);
        } catch (IOException e) {
            LOGGER.error("Failed to create script engine for bundle '{}': {}", info.getId(), e.getLocalizedMessage());
        }
    }

    private BundleFiles buildFileInfo(Path root) {
        Path assets = root.resolve("assets");
        Path data = root.resolve("data");
        List<Path> scripts = findScripts(root, assets, data);
        return new BundleFiles(root, assets, data, scripts);
    }

    private List<Path> findScripts(Path root, Path assets, Path data) {
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

    public Iterable<Bundle> getBundles() {
        return BUNDLES.values();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        BUNDLES.forEach(BundleScriptLoader::loadScripts);
    }
}