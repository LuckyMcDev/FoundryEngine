package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.info.BundleDependency;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.bundle.toml.BundleTomlParser;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import de.luckymcdev.foundryengine.server.Server;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Handles discovery, dependency sorting, and loading of bundles from the file system.
 */
public class BundleDiscovery {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final PathMatcher BUNDLES_FILE_MATCH =
            FileSystems.getDefault().getPathMatcher("glob:*.bundles.toml");
    private final Map<String, BundleInfo> discoveredBundles = new HashMap<>();
    private final List<PendingBundle> pendingBundles = new ArrayList<>();
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

        try (Stream<Path> stream = Files.list(directory)) {
            for (Path path : stream.toList()) {
                try {
                    if (Files.isDirectory(path)) {
                        scanDirectory(path, null);
                    } else if (path.toString().endsWith(".zip")) {
                        scanZip(path);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to scan bundle path: {}", path, e);
                    ModLoadingIssue issue = ModLoadingIssue.error(String.format(
                            "Failed to scan bundle path '%s': %s", path.getFileName(), e.getMessage()));
                    ModLoader.addLoadingIssue(issue);
                    if (Server.getServer() != null) {
                        Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] Bundle discovery '" + path.getFileName() + "': " + e), false);
                    }
                }
            }
        }

        List<PendingBundle> sortedBundles = sortBundles(pendingBundles);

        for (PendingBundle pending : sortedBundles) {
            loadBundle(pending.info(), pending.dir(), pending.fs());
        }

        LOGGER.info("Loaded {} Bundles.", discoveredBundles.size());
    }

    private void scanZip(Path zipPath) throws IOException {
        FileSystem zipFs = FileSystems.newFileSystem(zipPath, (ClassLoader) null);
        try {
            Path root = zipFs.getPath("/");
            if (!hasBundleToml(root)) {
                zipFs.close();
                return;
            }
            loadBundlesInDirectory(root, zipFs);
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

    private void scanDirectory(Path directory, @Nullable FileSystem zipFs) throws IOException {
        if (hasBundleToml(directory)) {
            loadBundlesInDirectory(directory, zipFs);
        }
    }

    private boolean hasBundleToml(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files.anyMatch(file -> BUNDLES_FILE_MATCH.matches(file.getFileName()));
        } catch (IOException e) {
            return false;
        }
    }

    private void loadBundlesInDirectory(Path directory, @Nullable FileSystem zipFs) {
        List<Path> bundleFiles = getBundleFiles(directory);
        if (bundleFiles.size() > 1) {
            throw new EngineException("More than one bundle file exists for bundle: " + directory);
        }
        bundleFiles.forEach(file -> {
            try {
                String content = Files.readString(file);
                List<BundleInfo> infos = BundleTomlParser.parse(content);
                for (BundleInfo info : infos) {
                    pendingBundles.add(new PendingBundle(info, directory, zipFs));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read bundle file: {}", file, e);
            }
        });
    }

    private List<Path> getBundleFiles(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(file -> BUNDLES_FILE_MATCH.matches(file.getFileName())).toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Performs a Topological Sort to handle bundle loading order.
     */
    private List<PendingBundle> sortBundles(List<PendingBundle> unsorted) {
        Map<String, PendingBundle> nodes = new HashMap<>();
        unsorted.forEach(p -> nodes.put(p.info().id(), p));

        List<PendingBundle> sorted = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();

        for (PendingBundle p : unsorted) {
            visit(p.info().id(), nodes, visiting, visited, sorted);
        }
        return sorted;
    }

    private void visit(String id, Map<String, PendingBundle> nodes, Set<String> visiting, Set<String> visited, List<PendingBundle> sorted) {
        if (visited.contains(id)) return;
        if (visiting.contains(id)) throw new EngineException("Circular bundle dependency detected: " + id);

        visiting.add(id);
        PendingBundle pending = nodes.get(id);

        if (pending != null) {
            for (BundleDependency dep : pending.info().dependencies()) {
                if (dep.type() == BundleDependency.Type.BUNDLE) {
                    visit(dep.id(), nodes, visiting, visited, sorted);
                }
            }
            sorted.add(pending);
        }

        visiting.remove(id);
        visited.add(id);
    }

    private void loadBundle(BundleInfo info, Path bundleDir, @Nullable FileSystem zipFs) {
        try {
            validateDependencies(info);

            Bundle bundle = bundleFactory.createBundle(info, bundleDir, zipFs);
            discoveredBundles.put(info.id(), info);
            bundleConsumer.accept(bundle);
        } catch (Exception e) {
            LOGGER.error("Failed to create bundle '{}': {}", info.id(), e.getMessage(), e);
            ModLoadingIssue issue = ModLoadingIssue.error(String.format(
                    "Failed to create bundle '%s': %s", info.id(), e.getMessage()));
            Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] " + issue), false);
        }
    }

    private void validateDependencies(BundleInfo info) {
        for (BundleDependency dep : info.dependencies()) {
            boolean satisfied = false;
            String currentVersionStr = "missing";

            if (dep.type() == BundleDependency.Type.MOD) {
                if (ModList.get().isLoaded(dep.id())) {
                    currentVersionStr = ModList.get().getModContainerById(dep.id())
                            .map(container -> container.getModInfo().getVersion().toString())
                            .orElse("unknown");
                    satisfied = isVersionSatisfied(dep.version(), currentVersionStr);
                }
            } else if (dep.type() == BundleDependency.Type.BUNDLE) {
                if (discoveredBundles.containsKey(dep.id())) {
                    currentVersionStr = discoveredBundles.get(dep.id()).versionInfo().toString();
                    satisfied = isVersionSatisfied(dep.version(), currentVersionStr);
                }
            }

            if (!satisfied) {
                String errorMsg = String.format(
                        "Bundle '%s' requires %s '%s' version '%s', but it is %s.",
                        info.id(),
                        dep.type().name().toLowerCase(),
                        dep.id(),
                        dep.version(),
                        currentVersionStr.equals("missing") ? "missing" : "version " + currentVersionStr
                );
                ModLoadingIssue issue = ModLoadingIssue.error(errorMsg);
                Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] " + issue), false);
            }
        }
    }

    private boolean isVersionSatisfied(String rangeSpec, String currentVersion) {
        if (rangeSpec.isEmpty() || rangeSpec.equalsIgnoreCase("any")) {
            return true;
        }

        try {
            ArtifactVersion current = new DefaultArtifactVersion(currentVersion);
            if (!rangeSpec.startsWith("[") && !rangeSpec.startsWith("(")) {
                ArtifactVersion required = new DefaultArtifactVersion(rangeSpec);
                return current.compareTo(required) >= 0;
            }
            VersionRange range = VersionRange.createFromVersionSpec(rangeSpec);
            return range.containsVersion(current);
        } catch (Exception e) {
            LOGGER.error("Failed to parse version range '{}' or version '{}'", rangeSpec, currentVersion);
            return false;
        }
    }

    public BundleFactory getBundleFactory() {
        return bundleFactory;
    }

    private record PendingBundle(BundleInfo info, Path dir, @Nullable FileSystem fs) {
    }
}