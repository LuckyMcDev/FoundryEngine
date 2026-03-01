package io.github.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import groovy.util.GroovyScriptEngine;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import io.github.luckymcdev.foundryengine.config.Config;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BundleScriptLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Changed return type from void to List<BundleEntrypoint>
    public static List<BundleEntrypoint> loadScripts(BundleFiles files, GroovyScriptEngine engine, IEventBus bundleBus, IEventBus eventBus, String bundleId) {
        List<BundleEntrypoint> liveEntrypoints = new ArrayList<>();

        if (!Config.SCRIPTING_ENABLED.get()) {
            LOGGER.info("Script loading is disabled in config.");
            return liveEntrypoints;
        }

        for (Path scriptPath : files.scripts()) {
            try {
                String scriptName = files.root().relativize(scriptPath).toString().replace('\\', '/');
                try (BufferedReader is = Files.newBufferedReader(scriptPath)) {
                    Class<?> clazz = engine.getGroovyClassLoader().parseClass(is, scriptName);

                    if (BundleEntrypoint.class.isAssignableFrom(clazz)) {
                        BundleEntrypoint script = (BundleEntrypoint) clazz
                                .getConstructor(IEventBus.class, IEventBus.class)
                                .newInstance(bundleBus, eventBus);

                        script.onLoad();
                        liveEntrypoints.add(script);
                        LOGGER.debug("Loaded entry point script '{}' for bundle '{}'", scriptName, bundleId);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load script '{}' for bundle '{}'", scriptPath, bundleId, e);
            }
        }

        return liveEntrypoints;
    }
}