package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleCreativeModeTab;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import de.luckymcdev.foundryengine.common.script.BundleScriptLoader;
import de.luckymcdev.foundryengine.common.script.ScriptEngineFactory;
import groovy.util.GroovyScriptEngine;
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

        BundleFiles files = BundleFiles.builder().build(bundleDir, zipFs);
        GroovyScriptEngine engine = scriptEngineFactory.create(files);
        BundleRegistryQuery registryQuery = new BundleRegistryQuery(info.id());

        BundleCreativeModeTab creativeTab = new BundleCreativeModeTab(info.id(), modBus, registryQuery);
        BundleConfig config = new BundleConfig(info.id(), configDirectory);

        List<BundleEntrypoint> entrypoints = scriptLoader.loadScripts(
                files, engine, modBus, eventBus, config, info.id()
        );

        return new Bundle(info, files, engine, registryQuery, eventBus, entrypoints, creativeTab, config);
    }

    public ScriptEngineFactory getScriptEngineFactory() {
        return scriptEngineFactory;
    }
}