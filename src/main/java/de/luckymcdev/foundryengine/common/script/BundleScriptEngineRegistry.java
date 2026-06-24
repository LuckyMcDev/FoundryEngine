package de.luckymcdev.foundryengine.common.script;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.server.Server;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Registry for {@link BundleScriptEngine} instances, keyed by file extension.
 */
public class BundleScriptEngineRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final GenericRegistry<String, BundleScriptEngine> engines = new GenericRegistry<>();

    public BundleScriptEngineRegistry() {
        register(new GroovyBundleScriptEngine());
    }

    /**
     * Registers a script engine for its file extension.
     */
    public void register(BundleScriptEngine engine) {
        engines.register(engine.fileExtension(), engine);
    }

    /**
     * Initializes all registered script engines for the given bundle files.
     */
    public void initializeAll(BundleFiles files) {
        engines.forEach(engine -> {
            try {
                engine.initialize(files);
            } catch (IOException e) {
                LOGGER.error("Failed to initialize {} script engine", engine.fileExtension(), e);
                ModLoadingIssue issue = ModLoadingIssue.error(String.format(
                        "Failed to initialize %s script engine: %s", engine.fileExtension(), e.getMessage()));
                ModLoader.addLoadingIssue(issue);
                if (Server.getServer() != null) {
                    String loc = e.getStackTrace().length > 0 ? " (" + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber() + ")" : "";
                    Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] Engine '" + engine.fileExtension() + "' initialization: " + e + loc), false);
                }
            }
        });
    }

    /**
     * Finds the appropriate script engine for the given file name by extension.
     */
    public BundleScriptEngine forFile(String fileName) {
        for (String ext : engines.keys()) {
            if (fileName.endsWith(ext)) {
                return engines.get(ext);
            }
        }
        throw new IllegalArgumentException("No script engine registered for file: " + fileName);
    }

    /**
     * Returns the list of all supported file extensions.
     */
    public List<String> supportedExtensions() {
        return List.copyOf(engines.keys());
    }

    /**
     * Closes all registered script engines.
     */
    public void closeAll() {
        engines.forEach(BundleScriptEngine::close);
    }
}