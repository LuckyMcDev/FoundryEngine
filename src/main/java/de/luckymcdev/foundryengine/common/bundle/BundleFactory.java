package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.RegistryEvent;
import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleCreativeModeTab;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import de.luckymcdev.foundryengine.common.script.BundleScriptLoader;
import de.luckymcdev.foundryengine.common.script.ScriptEngineFactory;
import groovy.util.GroovyScriptEngine;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Factory responsible for constructing Bundle instances with all their dependencies.
 */
public class BundleFactory {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ScriptEngineFactory scriptEngineFactory;
    private final BundleScriptLoader scriptLoader;
    private final IEventBus modBus;
    private final Path configDirectory;

    public BundleFactory(IEventBus modBus, Path configDirectory) {
        this.scriptEngineFactory = new ScriptEngineFactory();
        this.scriptLoader = new BundleScriptLoader();
        this.modBus = modBus;
        this.configDirectory = configDirectory;
    }

    public Bundle createBundle(BundleInfo info, Path bundleDir, @Nullable FileSystem zipFs) throws IOException {
        IEventBus eventBus = NeoForge.EVENT_BUS;
        IEventBus bundleBus = createBundleBus(info);
        bridgeEvents(info, bundleBus);

        BundleFiles files = BundleFiles.builder().build(bundleDir, zipFs);
        GroovyScriptEngine engine = scriptEngineFactory.create(files);
        BundleRegistryQuery registryQuery = new BundleRegistryQuery(info.id());
        BundleCreativeModeTab creativeTab = new BundleCreativeModeTab(info.id(), modBus, registryQuery);
        BundleConfig config = new BundleConfig(info.id(), configDirectory);

        List<BundleEntrypoint> entrypoints = scriptLoader.loadScripts(
                files, engine, bundleBus, eventBus, config, info.id()
        );

        return new Bundle(info, files, engine, registryQuery, eventBus, bundleBus, entrypoints, creativeTab, config);
    }

    private void bridgeEvents(BundleInfo info, IEventBus bundleBus) {
        registerRegistryBridge(info, bundleBus);
    }

    private void registerRegistryBridge(BundleInfo info, IEventBus bundleBus) {
        AtomicBoolean faulted = new AtomicBoolean(false);

        modBus.addListener((RegisterEvent event) -> {
            if (faulted.get()) return;
            try {
                bundleBus.post(new RegistryEvent(event, modBus));
            } catch (Throwable t) {
                if (faulted.compareAndSet(false, true)) {
                    LOGGER.error("Bundle '{}' faulted during RegistryEvent and will be skipped for remaining registries.",
                            info.id(), t);
                }
            }
        });
    }

    private IEventBus createBundleBus(BundleInfo info) {
        return BusBuilder.builder()
                .allowPerPhasePost()
                .setExceptionHandler((bus, event, listeners, index, throwable) -> {
                    // The bridge listener above handles logging and fault tracking.
                    // Re-throw so the try/catch in registerRegistryBridge can catch it.
                    if (throwable instanceof RuntimeException re) throw re;
                    throw new RuntimeException(throwable);
                })
                .build();
    }

    public ScriptEngineFactory getScriptEngineFactory() {
        return scriptEngineFactory;
    }
}