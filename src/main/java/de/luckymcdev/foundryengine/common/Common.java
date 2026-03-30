package de.luckymcdev.foundryengine.common;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.FoundryEngineMod;
import de.luckymcdev.foundryengine.common.bundle.BundleManager;
import de.luckymcdev.foundryengine.common.exceptions.UtilityClassException;
import de.luckymcdev.foundryengine.common.feature.FeatureManager;
import de.luckymcdev.foundryengine.common.game.behavior.GameBehaviorManager;
import de.luckymcdev.foundryengine.common.game.stage.GameStageHandler;
import de.luckymcdev.foundryengine.common.network.NetworkManager;
import de.luckymcdev.foundryengine.common.thread.ThreadManager;
import de.luckymcdev.foundryengine.common.util.FirstRun;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Common things for FoundryEngine.
 */
public abstract class Common {
    /**
     * Common Logger. Don't use, create your own.
     */
    public static final Logger LOGGER = LogUtils.getLogger();
    /**
     * Modid for FoundryEngine.
     */
    public static final String MODID = "foundryengine";
    /**
     * Mod Name
     */
    public static final String MODNAME = "FoundryEngine";

    public static final Path GAMEDIR = FMLPaths.GAMEDIR.get().normalize().toAbsolutePath();

    /**
     * Base Config Dir
     */
    public static final Path CONFIG = FMLPaths.CONFIGDIR.get();
    /**
     * FoundryEngine config dir
     */
    public static final Path FOUNDRY_ENGINE_CONFIG = CONFIG.resolve(MODID);
    /**
     * WIP database config dir.
     */
    public static final Path DATABASE_CONFIG = CONFIG.resolve("database");
    private static final boolean FIRST_RUN = FirstRun.isFor(MODID);
    public static final Path DIRECTORY = dir(GAMEDIR.resolve(MODNAME));
    public static final Path BUNDLES = dir(DIRECTORY.resolve("bundles"));
    public static final Path CACHE = dir(DIRECTORY.resolve(".cache"));
    public static final Path DUMPS = dir(CACHE.resolve("dumps"));
    public static final Path CONFIG_FE = dir(DIRECTORY.resolve("config"));
    private static final BundleManager BUNDLE_MANAGER = new BundleManager(FoundryEngineMod.getModBus(), CONFIG_FE);
    private static final ThreadManager THREAD_MANAGER = new ThreadManager();
    private static final GameBehaviorManager GAME_BEHAVIOR_MANAGER = new GameBehaviorManager();
    private static final GameStageHandler GAME_STAGE_HANDLER = new GameStageHandler();
    private static final FeatureManager FEATURE_MANAGER = new FeatureManager();
    private static final NetworkManager NETWORK_MANAGER = new NetworkManager();

    private Common() {
        throw new UtilityClassException();
    }

    /**
     * Returns an {@link Identifier} where the namespace is {@link #MODID}
     *
     * @param path the Path of the {@link Identifier}
     * @return returns the assembled {@link Identifier}
     */
    public static Identifier id(@NotNull String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static <V> Supplier<V> supOf(V value) {
        return () -> value;
    }


    public static ThreadManager getThreadManager() {
        return THREAD_MANAGER;
    }

    public static BundleManager getBundleManager() {
        return BUNDLE_MANAGER;
    }

    public static GameBehaviorManager getGameBehaviorManager() {
        return GAME_BEHAVIOR_MANAGER;
    }

    public static GameStageHandler getGameStageHandler() {
        return GAME_STAGE_HANDLER;
    }

    public static FeatureManager getFeatureManager() {
        return FEATURE_MANAGER;
    }

    public static NetworkManager getNetworkManager() {
        return NETWORK_MANAGER;
    }

    public static String getFileContent(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return new String(is.readAllBytes());
        } catch (Exception ex) {
            LOGGER.error("Failed to read file content: {}", file, ex);
            return "";
        }
    }

    // Event Bus

    public static <T extends Event> T post(T event) {
        return NeoForge.EVENT_BUS.post(event);
    }

    public static <T extends Event> T post(EventPriority priority, T event) {
        return NeoForge.EVENT_BUS.post(priority, event);
    }

    static Path dir(Path dir) {
        if (Files.notExists(dir) && FIRST_RUN) {
            try {
                Files.createDirectories(dir);
            } catch (Exception ex) {
                LOGGER.error(ex.getLocalizedMessage());
            }
        }
        return dir;
    }
}
