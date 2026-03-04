package io.github.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import groovy.util.GroovyScriptEngine;
import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import io.github.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import io.github.luckymcdev.foundryengine.common.bundle.toml.BundleTomlParser;
import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import io.github.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import io.github.luckymcdev.foundryengine.common.script.BundleScriptLoader;
import io.github.luckymcdev.foundryengine.common.script.ScriptEngineModifyEvent;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class BundleManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PathMatcher BUNDLES_FILE_MATCH = FileSystems.getDefault().getPathMatcher("glob:*.bundles.toml");
    private final GenericRegistry<String, Bundle> BUNDLES = new GenericRegistry<>();
    private int loadedBundles = 0;

    public BundleManager() {
    }

    public void register(Bundle bundle) {
        BUNDLES.register(bundle.info().id(), bundle);
        LOGGER.debug("Registered Bundle: {} with Info: {}", bundle.info().id(), bundle.info());
    }

    public void remove(Bundle bundle) {
        BUNDLES.remove(bundle.info().id());
        FileSystem fs = bundle.zipFileSystem();
        if (fs != null && fs.isOpen()) {
            try {
                fs.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to close ZIP FileSystem for bundle '{}': {}",
                        bundle.info().id(), e.getLocalizedMessage());
            }
        }
    }

    public void discover(Path in) throws IOException {
        LOGGER.debug("Discovering Bundles in: {}", in);
        try (Stream<Path> stream = Files.list(in)) {
            stream.forEach(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        checkBundle(path, null);
                    } else if (path.toString().endsWith(".zip")) {
                        discoverZip(path);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to check bundle: {}", path, e);
                }
            });
            LOGGER.info("Loaded {} Bundles.", loadedBundles);
        }
    }

    /**
     * Opens the ZIP FileSystem and intentionally does NOT close it here.
     * Ownership is transferred to the Bundle, which is closed via {@link #remove(Bundle)}.
     */
    private void discoverZip(Path zipPath) throws IOException {
        FileSystem zipFs = FileSystems.newFileSystem(zipPath, (ClassLoader) null);
        try {
            Path root = zipFs.getPath("/");
            checkBundle(root, zipFs);
        } catch (Exception e) {
            // If anything fails before the Bundle takes ownership, close it ourselves.
            try {
                zipFs.close();
            } catch (IOException ignore) {
            }
            throw e;
        }
    }

    public void checkBundle(Path in, FileSystem zipFs) throws IOException {
        if (!hasBundleToml(in)) {
            // No bundle here — close a ZIP FileSystem we opened for nothing.
            if (zipFs != null) {
                try {
                    zipFs.close();
                } catch (IOException ignore) {
                }
            }
            return;
        }
        LOGGER.debug("Found Bundle directory: {}", in);
        loadedBundles++;
        discoverInDirectory(in, zipFs);
    }

    private boolean hasBundleToml(Path dir) {
        try (Stream<Path> files = Files.list(dir)) {
            return files.anyMatch(file -> BUNDLES_FILE_MATCH.matches(file.getFileName()));
        } catch (IOException e) {
            return false;
        }
    }

    private void discoverInDirectory(Path dir, FileSystem zipFs) {
        List<Path> bundleFiles = getBundleFiles(dir);
        if (bundleFiles.size() > 1) {
            throw new RuntimeException("More than one bundle file exists for bundle: " + dir);
        }
        bundleFiles.forEach(file -> loadBundleFile(file, dir, zipFs));
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

    private void loadBundleFile(Path file, Path bundleDir, FileSystem zipFs) {
        try {
            String content = Files.readString(file);
            BundleTomlParser.parse(content).forEach(info -> loadBundle(info, bundleDir, zipFs));
        } catch (IOException e) {
            LOGGER.error("Failed to read bundle file: {}", file, e);
        }
    }

    private void loadBundle(BundleInfo info, Path bundleDir, @Nullable FileSystem zipFs) {
        try {
            BundleFiles files = buildFileInfo(bundleDir);

            URL[] roots = new URL[]{files.root().toUri().toURL(), files.generated().toUri().toURL()};

            GroovyScriptEngine engine = new GroovyScriptEngine(roots, FMLLoader.getCurrent().getCurrentClassLoader());

            CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

            Common.post(new ScriptEngineModifyEvent(engine, compilerConfiguration));
            engine.setConfig(compilerConfiguration);

            ImportCustomizer importCustomizer = new ImportCustomizer();
            compilerConfiguration.addCompilationCustomizers(importCustomizer);
            SecureASTCustomizer secure = new SecureASTCustomizer();
            secure.setClosuresAllowed(true);
            secure.setMethodDefinitionAllowed(true);
            secure.setDisallowedImports(
                    List.of("java.io.*", "java.net.*", "javax.*", "sun.*", "com.sun.*", "jdk.*")
            );
            secure.setDisallowedReceivers(
                    List.of("System", "Runtime", "Thread", "Class")
            );
            compilerConfiguration.addCompilationCustomizers(secure);

            IEventBus eventBus = NeoForge.EVENT_BUS;

            IEventBus bundleBus = BusBuilder.builder().allowPerPhasePost()
                    .setExceptionHandler((bus, event, listeners, index, throwable) -> {
                        LOGGER.error("Bundle '{}' faulted during event {}: {}", info.id(), event.getClass().getSimpleName(), throwable.getMessage());
                    })
                    .build();

            List<BundleEntrypoint> entrypoints = new ArrayList<>();

            entrypoints.addAll(BundleScriptLoader.loadScripts(files, engine, bundleBus, eventBus, info.id()));

            var registryQuery = new BundleRegistryQuery(info.id());

            Bundle bundle = new Bundle(info, files, engine, registryQuery, eventBus, bundleBus, entrypoints, zipFs);

            register(bundle);
        } catch (IOException e) {
            LOGGER.error("Failed to create script engine for bundle '{}': {}", info.id(), e.getLocalizedMessage());
        }
    }

    private BundleFiles buildFileInfo(Path root) {
        Path assets = root.resolve("assets");
        Path data = root.resolve("data");
        Path generated = root.resolve("generated");
        List<Path> scripts = findScripts(root, assets, data);
        return new BundleFiles(root, assets, generated, data, scripts);
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

    public void reload() {
        LOGGER.info("Reloading FoundryEngine Bundles...");

        for (Bundle bundle : BUNDLES.values()) {
            for (BundleEntrypoint ep : bundle.entrypoints()) {
                try {
                    ep.onUnload();
                } catch (Exception e) {
                    LOGGER.error("Error unloading script in bundle {}", bundle.info().id(), e);
                }
            }

            if (bundle.zipFileSystem() != null) {
                try {
                    bundle.zipFileSystem().close();
                } catch (IOException ignored) {
                }
            }
        }

        this.BUNDLES.clear();
        this.loadedBundles = 0;

        try {
            this.discover(Common.BUNDLES);
        } catch (IOException e) {
            LOGGER.error("Failed to reload bundles", e);
        }
    }

    @Override
    public void onResourceManagerReload(@NonNull ResourceManager resourceManager) {
        reload();
    }
}