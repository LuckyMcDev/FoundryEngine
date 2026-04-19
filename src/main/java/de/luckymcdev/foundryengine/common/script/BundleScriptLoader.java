package de.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.priority.Priority;
import de.luckymcdev.foundryengine.config.StartupConfig;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BundleScriptLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public List<BundleEntrypoint> loadCommon(BundleFiles files, BundleScriptEngineRegistry registry) {
        return load(files, registry, BundleFiles.ScriptFiles::common, "common");
    }

    public List<BundleEntrypoint> loadClient(BundleFiles files, BundleScriptEngineRegistry registry) {
        return load(files, registry, BundleFiles.ScriptFiles::client, "client");
    }

    public List<BundleEntrypoint> loadServer(BundleFiles files, BundleScriptEngineRegistry registry) {
        return load(files, registry, BundleFiles.ScriptFiles::server, "server");
    }

    private List<BundleEntrypoint> load(BundleFiles files, BundleScriptEngineRegistry registry,
                                        Function<BundleFiles.ScriptFiles, Path> pathGetter, String envName) {
        List<BundleEntrypoint> entrypoints = new ArrayList<>();

        if (!StartupConfig.SCRIPTING_ENABLED.get()) {
            return entrypoints;
        }

        Path envPath = pathGetter.apply(files.scripts());
        List<String> supported = registry.supportedExtensions();

        List<Path> scriptPaths = files.scripts().collection().stream()
                .filter(p -> p.startsWith(envPath))
                .filter(p -> supported.stream().anyMatch(ext -> p.toString().endsWith(ext)))
                .toList();

        for (Path scriptPath : scriptPaths) {
            try {
                BundleEntrypoint entrypoint = loadScriptClass(scriptPath, files, registry);
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

    private @Nullable BundleEntrypoint loadScriptClass(Path scriptPath, BundleFiles files,
                                                       BundleScriptEngineRegistry registry) throws Exception {
        String scriptName = files.scripts().root().relativize(scriptPath).toString().replace('\\', '/');
        BundleScriptEngine engine = registry.forFile(scriptName);
        Class<?> scriptClass = engine.loadClass(scriptName);

        if (BundleEntrypoint.class.isAssignableFrom(scriptClass)) {
            return (BundleEntrypoint) scriptClass.getDeclaredConstructor().newInstance();
        }

        return null;
    }
}