package de.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.server.Server;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BundleScriptEngineRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<String, BundleScriptEngine> engines = new LinkedHashMap<>();

    public BundleScriptEngineRegistry() {
        register(new GroovyBundleScriptEngine());
    }

    public void register(BundleScriptEngine engine) {
        engines.put(engine.fileExtension(), engine);
    }

    public void initializeAll(BundleFiles files) {
        for (BundleScriptEngine engine : engines.values()) {
            try {
                engine.initialize(files);
            } catch (IOException e) {
                LOGGER.error("Failed to initialize {} script engine", engine.fileExtension(), e);
                ModLoadingIssue issue = ModLoadingIssue.error(String.format(
                        "Failed to initialize %s script engine: %s", engine.fileExtension(), e.getMessage()));
                ModLoader.addLoadingIssue(issue);
                if (Server.getServer() != null) {
                    Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] Engine '" + engine.fileExtension() + "' initialization: " + e), false);
                }
            }
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