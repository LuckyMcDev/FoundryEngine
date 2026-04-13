package de.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.priority.Priority;
import de.luckymcdev.foundryengine.config.StartupConfig;
import groovy.util.GroovyScriptEngine;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BundleScriptLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public List<BundleEntrypoint> loadCommon(BundleFiles files, GroovyScriptEngine engine) {
        return load(files, engine, BundleFiles.ScriptFiles::common, "common");
    }

    public List<BundleEntrypoint> loadClient(BundleFiles files, GroovyScriptEngine engine) {
        return load(files, engine, BundleFiles.ScriptFiles::client, "client");
    }

    public List<BundleEntrypoint> loadServer(BundleFiles files, GroovyScriptEngine engine) {
        return load(files, engine, BundleFiles.ScriptFiles::server, "server");
    }

    private List<BundleEntrypoint> load(BundleFiles files, GroovyScriptEngine engine, Function<BundleFiles.ScriptFiles, Path> pathGetter, String envName) {
        List<BundleEntrypoint> entrypoints = new ArrayList<>();

        if (!StartupConfig.SCRIPTING_ENABLED.get()) {
            return entrypoints;
        }

        Path envPath = pathGetter.apply(files.scripts());

        List<Path> scriptPaths = files.scripts().collection().stream()
                .filter(p -> p.startsWith(envPath))
                .toList();

        for (Path scriptPath : scriptPaths) {
            try {
                BundleEntrypoint entrypoint = loadScriptClass(scriptPath, files, engine);
                if (entrypoint != null) {
                    entrypoints.add(entrypoint);
                    entrypoint.onLoad();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load {} script '{}'", envName, scriptPath.getFileName(), e);
            }
        }

        entrypoints.sort(Priority.comparing(BundleEntrypoint::getPriority));
        return entrypoints;
    }

    private @Nullable BundleEntrypoint loadScriptClass(Path scriptPath, BundleFiles files, GroovyScriptEngine engine) throws Exception {
        String scriptName = files.scripts().root().relativize(scriptPath).toString().replace('\\', '/');
        Class<?> scriptClass = engine.loadScriptByName(scriptName);

        if (BundleEntrypoint.class.isAssignableFrom(scriptClass)) {
            return (BundleEntrypoint) scriptClass.getDeclaredConstructor().newInstance();
        }

        return null;
    }
}