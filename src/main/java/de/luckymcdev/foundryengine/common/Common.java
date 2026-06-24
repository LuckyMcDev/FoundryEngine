package de.luckymcdev.foundryengine.common;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.FoundryEngineMod;
import de.luckymcdev.foundryengine.common.area.AreaManager;
import de.luckymcdev.foundryengine.common.bundle.BundleManager;
import de.luckymcdev.foundryengine.common.bundle.BundleSavePathListener;
import de.luckymcdev.foundryengine.common.cutscene.CutsceneManager;
import de.luckymcdev.foundryengine.common.cutscene.CutsceneSessionManager;
import de.luckymcdev.foundryengine.common.exceptions.UtilityClassException;
import de.luckymcdev.foundryengine.common.game.GameManager;
import de.luckymcdev.foundryengine.common.game.stage.GameStageHandler;
import de.luckymcdev.foundryengine.common.network.NetworkManager;
import de.luckymcdev.foundryengine.common.savedata.SavedDataManager;
import de.luckymcdev.foundryengine.common.util.FirstRun;
import de.luckymcdev.foundryengine.common.waypoint.WaypointManager;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.SystemProperties;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared constants and singleton managers for FoundryEngine.
 */
public final class Common {
    public static final String MODID = "foundryengine";
    public static final String MODNAME = "FoundryEngine";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Path GAMEDIR = FMLPaths.GAMEDIR.get().normalize().toAbsolutePath();
    public static final Path CONFIG = FMLPaths.CONFIGDIR.get();
    public static final Path TEMP_DIR = Path.of(SystemProperties.getProperty("java.io.tmpdir")).resolve(MODID);
    private static final boolean FIRST_RUN = FirstRun.isFor(MODID);
    public static final Path DIRECTORY = dir(GAMEDIR.resolve(MODNAME));
    public static final Path BUNDLES = dir(DIRECTORY.resolve("bundles"));
    public static final Path CACHE = dir(DIRECTORY.resolve(".cache"));
    public static final Path DUMPS = dir(CACHE.resolve("dumps"));
    public static final Path GAME = dir(CACHE.resolve("game"));
    public static final Path CONFIG_FE = dir(DIRECTORY.resolve("config"));
    private static final BundleManager BUNDLE_MANAGER = new BundleManager(FoundryEngineMod.getModBus(), CONFIG_FE);
    private static final GameStageHandler GAME_STAGE_HANDLER = new GameStageHandler();
    private static final NetworkManager NETWORK_MANAGER = new NetworkManager();
    private static final AreaManager AREA_MANAGER = new AreaManager();
    private static final CutsceneManager CUTSCENE_MANAGER = new CutsceneManager();
    private static final CutsceneSessionManager CUTSCENE_SESSION_MANAGER = new CutsceneSessionManager();
    private static final SavedDataManager SAVED_DATA_MANAGER = new SavedDataManager();
    private static final WaypointManager WAYPOINT_MANAGER = new WaypointManager();
    private static final GameManager GAME_MANAGER = new GameManager();
    private static final List<Runnable> EVENT_CLEARERS = new ArrayList<>();

    static {
        BUNDLE_MANAGER.getLifecycleDispatcher().register(new BundleSavePathListener());
        BUNDLE_MANAGER.getLifecycleDispatcher().register(GAME_MANAGER);
    }

    private Common() {
        throw new UtilityClassException();
    }

    /**
     * Returns a {@link Identifier} namespaced to {@link #MODID}.
     */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    /**
     * Returns an {@link Identifier} in the default Minecraft namespace.
     */
    public static Identifier mId(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    /**
     * Returns an {@link Identifier} with the given namespace and path.
     */
    public static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    /**
     * Returns the singleton {@link BundleManager}.
     */
    public static BundleManager getBundleManager() {
        return BUNDLE_MANAGER;
    }

    /**
     * Returns the singleton {@link GameStageHandler}.
     */
    public static GameStageHandler getGameStageHandler() {
        return GAME_STAGE_HANDLER;
    }

    /**
     * Returns the singleton {@link NetworkManager}.
     */
    public static NetworkManager getNetworkManager() {
        return NETWORK_MANAGER;
    }

    /**
     * Returns the singleton {@link AreaManager}.
     */
    public static AreaManager getAreaManager() {
        return AREA_MANAGER;
    }

    /**
     * Returns the singleton {@link CutsceneManager}.
     */
    public static CutsceneManager getCutsceneManager() {
        return CUTSCENE_MANAGER;
    }

    /**
     * Returns the singleton {@link CutsceneSessionManager}.
     */
    public static CutsceneSessionManager getCutsceneSessionManager() {
        return CUTSCENE_SESSION_MANAGER;
    }

    /**
     * Returns the singleton {@link SavedDataManager}.
     */
    public static SavedDataManager getSavedDataManager() {
        return SAVED_DATA_MANAGER;
    }

    /**
     * Returns the singleton {@link WaypointManager}.
     */
    public static WaypointManager getWaypointManager() {
        return WAYPOINT_MANAGER;
    }

    /**
     * Returns the singleton {@link GameManager}.
     */
    public static GameManager getGameManager() {
        return GAME_MANAGER;
    }

    /**
     * Reads the full content of a file as a UTF-8 string. Returns empty on failure.
     */
    public static String getFileContent(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to read file: {}", file, e);
            return "";
        }
    }

    /**
     * Posts an event to the NeoForge event bus.
     */
    public static <T extends Event> T post(T event) {
        return NeoForge.EVENT_BUS.post(event);
    }

    /**
     * Posts an event at the given priority to the NeoForge event bus.
     */
    public static <T extends Event> T post(EventPriority priority, T event) {
        return NeoForge.EVENT_BUS.post(priority, event);
    }

    /**
     * Registers a cleanup callback invoked on {@link #clearEvents()}.
     */
    public static void registerEventClear(Runnable clearer) {
        EVENT_CLEARERS.add(clearer);
    }

    /**
     * Runs all registered event cleanup callbacks.
     */
    public static void clearEvents() {
        for (var clearer : EVENT_CLEARERS) {
            clearer.run();
        }
    }

    static Path dir(Path path) {
        if (Files.notExists(path) && FIRST_RUN) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return path;
    }
}