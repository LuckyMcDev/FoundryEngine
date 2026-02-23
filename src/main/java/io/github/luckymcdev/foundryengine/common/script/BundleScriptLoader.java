package io.github.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import groovy.util.GroovyScriptEngine;
import io.github.luckymcdev.foundryengine.common.bundle.Bundle;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class BundleScriptLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void loadScripts(Bundle bundle) {
        BundleFiles files = bundle.bundleFiles();
        GroovyScriptEngine engine = bundle.scriptEngine();
        IEventBus eventBus = bundle.eventBus();
        String bundleId = bundle.info().getId();

        LOGGER.debug("Loading scripts for bundle '{}', found {} scripts", bundleId, files.getScriptCount());

        for (Path scriptPath : files.scripts()) {
            try {
                LOGGER.debug("Attempting to load script: {}", scriptPath);
                String scriptName = files.root().relativize(scriptPath).toString().replace('\\', '/');

                try (BufferedReader is = Files.newBufferedReader(scriptPath)) {
                    Class<?> clazz = engine.getGroovyClassLoader().parseClass(is, scriptName);

                    LOGGER.debug("Loaded class: {}, is BundleEntrypoint: {}",
                            clazz.getName(), BundleEntrypoint.class.isAssignableFrom(clazz));

                    if (BundleEntrypoint.class.isAssignableFrom(clazz)) {
                        BundleEntrypoint script = (BundleEntrypoint) clazz
                                .getConstructor(IEventBus.class)
                                .newInstance(eventBus);
                        script.onLoad();
                        LOGGER.debug("Loaded entry point script '{}' for bundle '{}'", scriptName, bundleId);
                    } else {
                        LOGGER.debug("Script '{}' does not extend BundleEntrypoint, skipping", scriptName);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load script '{}' for bundle '{}'", scriptPath, bundleId, e);
            }
        }
    }
}