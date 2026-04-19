package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BundleScriptEngineRegistry {

    private final Map<String, BundleScriptEngine> engines = new LinkedHashMap<>();

    public BundleScriptEngineRegistry() {
        register(new GroovyBundleScriptEngine());
    }

    public void register(BundleScriptEngine engine) {
        engines.put(engine.fileExtension(), engine);
    }

    public void initializeAll(BundleFiles files) throws IOException {
        for (BundleScriptEngine engine : engines.values()) {
            engine.initialize(files);
        }
    }

    public BundleScriptEngine forFile(String fileName) {
        for (Map.Entry<String, BundleScriptEngine> entry : engines.entrySet()) {
            if (fileName.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("No script engine registered for file: " + fileName);
    }

    public List<String> supportedExtensions() {
        return new ArrayList<>(engines.keySet());
    }

    public void closeAll() {
        engines.values().forEach(BundleScriptEngine::close);
    }
}