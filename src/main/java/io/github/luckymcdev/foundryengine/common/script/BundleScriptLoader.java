package io.github.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import groovy.util.GroovyScriptEngine;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import io.github.luckymcdev.foundryengine.config.Config;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced script loader that leverages GroovyScriptEngine's caching and reloading capabilities.
 */
public class BundleScriptLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Loads all scripts from the bundle using the GroovyScriptEngine.
     *
     * @param files     Bundle file information
     * @param engine    Configured GroovyScriptEngine
     * @param bundleBus Bundle-specific event bus
     * @param eventBus  Global event bus
     * @param bundleId  Bundle identifier for logging
     * @return List of loaded entrypoint instances
     */
    public List<BundleEntrypoint> loadScripts(BundleFiles files, GroovyScriptEngine engine, IEventBus bundleBus, IEventBus eventBus, String bundleId) {
        List<BundleEntrypoint> entrypoints = new ArrayList<>();

        if (!Config.Startup.SCRIPTING_ENABLED.get()) {
            LOGGER.info("Script loading is disabled in config.");
            return entrypoints;
        }

        for (Path scriptPath : files.scripts()) {
            try {
                BundleEntrypoint entrypoint = loadScript(
                        scriptPath,
                        files,
                        engine,
                        bundleBus,
                        eventBus,
                        bundleId
                );

                if (entrypoint != null) {
                    entrypoints.add(entrypoint);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load script '{}' for bundle '{}'", scriptPath, bundleId, e);
            }
        }

        return entrypoints;
    }

    /**
     * Loads a single script using GroovyScriptEngine's run() method.
     * This leverages caching and proper class reloading.
     */
    private BundleEntrypoint loadScript(Path scriptPath, BundleFiles files, GroovyScriptEngine engine, IEventBus bundleBus, IEventBus eventBus, String bundleId) throws Exception {
        String scriptName = getRelativeScriptName(files.root(), scriptPath);

        Class<?> scriptClass = engine.loadScriptByName(scriptName);
        if (BundleEntrypoint.class.isAssignableFrom(scriptClass)) {
            BundleEntrypoint entrypoint = instantiateEntrypoint(scriptClass, bundleBus, eventBus);
            entrypoint.onLoad();
            LOGGER.debug("Loaded entrypoint class '{}' for bundle '{}'", scriptName, bundleId);
            return entrypoint;
        }

        LOGGER.debug("Script '{}' is not a BundleEntrypoint, skipping", scriptName);
        return null;
    }

    /**
     * Instantiates a BundleEntrypoint from a class using the expected constructor.
     */
    private BundleEntrypoint instantiateEntrypoint(Class<?> clazz, IEventBus bundleBus, IEventBus eventBus) throws Exception {
        try {
            return (BundleEntrypoint) clazz
                    .getConstructor(IEventBus.class, IEventBus.class)
                    .newInstance(bundleBus, eventBus);
        } catch (NoSuchMethodException e) {
            try {
                BundleEntrypoint instance = (BundleEntrypoint) clazz.getConstructor().newInstance();
                LOGGER.warn("Script class {} uses no-arg constructor. Consider using (IEventBus, IEventBus) constructor.",
                        clazz.getSimpleName());
                return instance;
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(
                        "BundleEntrypoint class must have either (IEventBus, IEventBus) or no-arg constructor: "
                                + clazz.getName(), ex);
            }
        }
    }

    /**
     * Converts an absolute script path to a relative path for GroovyScriptEngine.
     */
    private String getRelativeScriptName(Path root, Path scriptPath) {
        return root.relativize(scriptPath).toString().replace('\\', '/');
    }

    /**
     * Reloads a specific script using GroovyScriptEngine's reloading capabilities.
     * This is useful for hot-reloading during development.
     */
    public BundleEntrypoint reloadScript(Path scriptPath, BundleFiles files, GroovyScriptEngine engine, IEventBus bundleBus, IEventBus eventBus, String bundleId) throws Exception {
        String scriptName = getRelativeScriptName(files.root(), scriptPath);

        // Clear cached script if it exists (forces reload)
        engine.getGroovyClassLoader().clearCache();

        return loadScript(scriptPath, files, engine, bundleBus, eventBus, bundleId);
    }
}