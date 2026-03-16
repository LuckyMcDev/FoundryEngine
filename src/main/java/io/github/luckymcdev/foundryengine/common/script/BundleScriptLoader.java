package io.github.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import io.github.luckymcdev.foundryengine.common.exeptions.EngineException;
import io.github.luckymcdev.foundryengine.common.priority.Priority;
import io.github.luckymcdev.foundryengine.config.Config;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Script Loader for a Bundle.
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
     * @return List of loaded entrypoint instances (in priority order)
     */
    public List<BundleEntrypoint> loadScripts(BundleFiles files, GroovyScriptEngine engine, IEventBus bundleBus, IEventBus eventBus, String bundleId) {
        List<BundleEntrypoint> entrypoints = new ArrayList<>();

        if (!Config.Startup.SCRIPTING_ENABLED.get()) {
            LOGGER.info("Script loading is disabled in config.");
            return entrypoints;
        }

        if (files.scripts().isEmpty()) {
            LOGGER.debug("Bundle '{}' has no scripts", bundleId);
            return entrypoints;
        }

        LOGGER.debug("Loading {} script(s) for bundle '{}'", files.scripts().size(), bundleId);

        for (Path scriptPath : files.scripts()) {
            try {
                BundleEntrypoint entrypoint = loadScriptClass(
                        scriptPath,
                        files,
                        engine,
                        bundleBus,
                        eventBus
                );

                if (entrypoint != null) {
                    entrypoints.add(entrypoint);
                    LOGGER.debug("Loaded entrypoint from '{}' with priority {}",
                            scriptPath.getFileName(), entrypoint.getPriority());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load script '{}' for bundle '{}'",
                        scriptPath.getFileName(), bundleId, e);
            }
        }

        if (entrypoints.isEmpty()) {
            LOGGER.debug("Bundle '{}' has no entrypoints (scripts may not implement BundleEntrypoint)", bundleId);
            return entrypoints;
        }

        entrypoints.sort(Priority.comparing(BundleEntrypoint::getPriority));

        LOGGER.debug("Sorted {} entrypoint(s) by priority for bundle '{}'", entrypoints.size(), bundleId);

        for (int i = 0; i < entrypoints.size(); i++) {
            BundleEntrypoint ep = entrypoints.get(i);
            try {
                LOGGER.debug("Initializing entrypoint {}/{} ({}) for bundle '{}'",
                        i + 1, entrypoints.size(), ep.getPriority(), bundleId);
                ep.onLoad();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize entrypoint {} (priority: {}) for bundle '{}'",
                        ep.getClass().getSimpleName(), ep.getPriority(), bundleId, e);
            }
        }

        LOGGER.info("Loaded and initialized {} entrypoint(s) for bundle '{}'", entrypoints.size(), bundleId);
        return entrypoints;
    }

    /**
     * Loads a single script class and instantiates it as a BundleEntrypoint.
     * Does NOT call onLoad() - that happens later in priority order.
     */
    private BundleEntrypoint loadScriptClass(Path scriptPath, BundleFiles files, GroovyScriptEngine engine, IEventBus bundleBus, IEventBus eventBus) throws InvocationTargetException, InstantiationException, IllegalAccessException, ResourceException, ScriptException {
        String scriptName = getRelativeScriptName(files.root(), scriptPath);

        Class<?> scriptClass = engine.loadScriptByName(scriptName);
        if (BundleEntrypoint.class.isAssignableFrom(scriptClass)) {
            return instantiateEntrypoint(scriptClass, bundleBus, eventBus);
        }

        LOGGER.trace("Script '{}' is not a BundleEntrypoint, skipping", scriptName);
        return null;
    }

    /**
     * Instantiates a BundleEntrypoint from a class using the expected constructor.
     */
    private BundleEntrypoint instantiateEntrypoint(Class<?> clazz, IEventBus bundleBus, IEventBus eventBus) throws InvocationTargetException, InstantiationException, IllegalAccessException {
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
                throw new EngineException(
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
        BundleEntrypoint entrypoint = loadScriptClass(scriptPath, files, engine, bundleBus, eventBus);
        if (entrypoint != null) {
            LOGGER.info("Hot-reloading script '{}' for bundle '{}'", scriptName, bundleId);
            entrypoint.onLoad();
        }
        return entrypoint;
    }
}