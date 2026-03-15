package io.github.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import groovy.util.GroovyScriptEngine;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import io.github.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import io.github.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import io.github.luckymcdev.foundryengine.common.script.BundleScriptLoader;
import io.github.luckymcdev.foundryengine.common.script.ScriptEngineFactory;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

/**
 * Factory responsible for constructing Bundle instances with all their dependencies.
 */
public class BundleFactory {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ScriptEngineFactory scriptEngineFactory;
    private final BundleScriptLoader scriptLoader;

    public BundleFactory() {
        this.scriptEngineFactory = new ScriptEngineFactory();
        this.scriptLoader = new BundleScriptLoader();
    }

    /**
     * Creates a complete Bundle from the given info and directory.
     *
     * @param info      Bundle metadata
     * @param bundleDir Root directory of the bundle
     * @param zipFs     Optional ZIP FileSystem (null for folder bundles)
     * @return A fully initialized Bundle
     * @throws IOException if bundle construction fails
     */
    public Bundle createBundle(BundleInfo info, Path bundleDir, @Nullable FileSystem zipFs) throws IOException {
        BundleFiles files = BundleFilesBuilder.build(bundleDir);

        GroovyScriptEngine engine = scriptEngineFactory.create(files);

        IEventBus eventBus = NeoForge.EVENT_BUS;
        IEventBus bundleBus = createBundleBus(info);

        List<BundleEntrypoint> entrypoints = scriptLoader.loadScripts(
                files,
                engine,
                bundleBus,
                eventBus,
                info.id()
        );

        BundleRegistryQuery registryQuery = new BundleRegistryQuery(info.id());

        return new Bundle(info, files, engine, registryQuery, eventBus, bundleBus, entrypoints, zipFs);
    }

    private IEventBus createBundleBus(BundleInfo info) {
        return BusBuilder.builder()
                .allowPerPhasePost()
                .setExceptionHandler((bus, event, listeners, index, throwable) -> {
                    LOGGER.error("Bundle '{}' faulted during event {}: {}",
                            info.id(),
                            event.getClass().getSimpleName(),
                            throwable.getMessage());
                })
                .build();
    }

    public ScriptEngineFactory getScriptEngineFactory() {
        return scriptEngineFactory;
    }
}