package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.RegistryEvent;
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

/**
 * Factory responsible for constructing Bundle instances with all their dependencies.
 */
public class BundleFactory {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ScriptEngineFactory scriptEngineFactory;
    private final BundleScriptLoader scriptLoader;
    private final IEventBus modBus;

    public BundleFactory(IEventBus modBus) {
        this.scriptEngineFactory = new ScriptEngineFactory();
        this.scriptLoader = new BundleScriptLoader();
        this.modBus = modBus;
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
        BundleFiles files = BundleFilesBuilder.build(bundleDir, zipFs);
        GroovyScriptEngine engine = scriptEngineFactory.create(files);

        IEventBus eventBus = NeoForge.EVENT_BUS;
        IEventBus bundleBus = createBundleBus(info);

        bridgeEvents(bundleBus);

        BundleRegistryQuery registryQuery = new BundleRegistryQuery(info.id());

        BundleCreativeModeTab creativeTab = new BundleCreativeModeTab(info.id(), modBus, registryQuery);

        List<BundleEntrypoint> entrypoints = scriptLoader.loadScripts(
                files, engine, bundleBus, eventBus, info.id()
        );

        return new Bundle(info, files, engine, registryQuery, eventBus, bundleBus, entrypoints, creativeTab);
    }

    private void bridgeEvents(IEventBus bundleBus) {
        registerRegistryBridge(bundleBus);
    }

    /**
     * Bridges NeoForge's RegisterEvent on the global bus into FoundryEngine's
     * RegistryEvent on the bundle bus, so scripts can use the cleaner API.
     */
    private void registerRegistryBridge(IEventBus bundleBus) {
        modBus.addListener((RegisterEvent event) -> bundleBus.post(new RegistryEvent(event)));
    }

    private IEventBus createBundleBus(BundleInfo info) {
        return BusBuilder.builder()
                .allowPerPhasePost()
                .setExceptionHandler((bus, event, listeners, index, throwable) ->
                        LOGGER.error("Bundle '{}' faulted during event {}: {}",
                                info.id(),
                                event.getClass().getSimpleName(),
                                throwable.getMessage()))
                .build();
    }

    public ScriptEngineFactory getScriptEngineFactory() {
        return scriptEngineFactory;
    }
}