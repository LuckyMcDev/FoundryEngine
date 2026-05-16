package de.luckymcdev.foundryengine.common;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.FoundryEngineMod;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.area.AreaManager;
import de.luckymcdev.foundryengine.common.blueprint.BlueprintManager;
import de.luckymcdev.foundryengine.common.bundle.BundleManager;
import de.luckymcdev.foundryengine.common.cutscene.CutsceneManager;
import de.luckymcdev.foundryengine.common.cutscene.CutsceneSessionManager;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import de.luckymcdev.foundryengine.common.exceptions.UtilityClassException;
import de.luckymcdev.foundryengine.common.game.stage.GameStageHandler;
import de.luckymcdev.foundryengine.common.network.NetworkManager;
import de.luckymcdev.foundryengine.common.savedata.SavedDataManager;
import de.luckymcdev.foundryengine.common.util.FirstRun;
import de.luckymcdev.foundryengine.common.util.ini.IniFile;
import de.luckymcdev.foundryengine.common.util.ini.IniFileManager;
import de.luckymcdev.foundryengine.common.waypoint.WaypointManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.SystemProperties;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Shared constants and singleton managers for FoundryEngine.
 */
public abstract class Common {
    public static final String MODID = "foundryengine";
    public static final String MODNAME = "FoundryEngine";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Path GAMEDIR = FMLPaths.GAMEDIR.get().normalize().toAbsolutePath();
    public static final Path CONFIG = FMLPaths.CONFIGDIR.get();
    public static final Path TEMP_DIR = Path.of(SystemProperties.getProperty("java.io.tmpdir")).resolve(MODID);
    public static final IniFile INI_FILE;
    private static final boolean FIRST_RUN = FirstRun.isFor(MODID);
    public static final Path DIRECTORY = dir(GAMEDIR.resolve(MODNAME));
    public static final Path BUNDLES = dir(DIRECTORY.resolve("bundles"));
    public static final Path CACHE = dir(DIRECTORY.resolve(".cache"));
    public static final Path DUMPS = dir(CACHE.resolve("dumps"));
    public static final Path CONFIG_FE = dir(DIRECTORY.resolve("config"));
    private static final BundleManager BUNDLE_MANAGER = new BundleManager(FoundryEngineMod.getModBus(), CONFIG_FE);
    public static final Path INIFILEPATH = file(DIRECTORY.resolve("foundryengine.ini"));
    private static final GameStageHandler GAME_STAGE_HANDLER = new GameStageHandler();
    private static final NetworkManager NETWORK_MANAGER = new NetworkManager();
    private static final BlueprintManager BLUEPRINT_MANAGER = new BlueprintManager();
    private static final AreaManager AREA_MANAGER = new AreaManager();
    private static final CutsceneManager CUTSCENE_MANAGER = new CutsceneManager();
    private static final CutsceneSessionManager CUTSCENE_SESSION_MANAGER = new CutsceneSessionManager();
    private static final SavedDataManager SAVED_DATA_MANAGER = new SavedDataManager();
    private static final WaypointManager WAYPOINT_MANAGER = new WaypointManager();
    private static final IniFileManager INI_FILE_MANAGER;

    static {
        try {
            INI_FILE = new IniFile(INIFILEPATH);
            INI_FILE_MANAGER = new IniFileManager(INI_FILE);
        } catch (IOException e) {
            throw new EngineException(e);
        }
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

    public static <V> Supplier<V> supOf(V value) {
        return () -> value;
    }

    public static BundleManager getBundleManager() {
        return BUNDLE_MANAGER;
    }

    public static GameStageHandler getGameStageHandler() {
        return GAME_STAGE_HANDLER;
    }

    public static NetworkManager getNetworkManager() {
        return NETWORK_MANAGER;
    }

    public static BlueprintManager getBlueprintManager() {
        return BLUEPRINT_MANAGER;
    }

    public static AreaManager getAreaManager() {
        return AREA_MANAGER;
    }

    public static CutsceneManager getCutsceneManager() {
        return CUTSCENE_MANAGER;
    }

    public static CutsceneSessionManager getCutsceneSessionManager() {
        return CUTSCENE_SESSION_MANAGER;
    }

    public static SavedDataManager getSavedDataManager() {
        return SAVED_DATA_MANAGER;
    }

    public static WaypointManager getWaypointManager() {
        return WAYPOINT_MANAGER;
    }

    public static @Nullable RecipeManager getRecipeManager() {
        if (FMLEnvironment.getDist().isClient()) {
            Minecraft mc = Client.getMc();
            if (mc.getSingleplayerServer() != null) {
                return mc.getSingleplayerServer().getRecipeManager();
            }
        } else {
            var server = ServerLifecycleHooks.getCurrentServer();
            return server != null ? server.getRecipeManager() : null;
        }
        return null;
    }

    public static String getFileContent(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            return new String(is.readAllBytes());
        } catch (Exception e) {
            LOGGER.error("Failed to read file: {}", file, e);
            return "";
        }
    }

    public static <T extends Event> T post(T event) {
        return NeoForge.EVENT_BUS.post(event);
    }

    public static <T extends Event> T post(EventPriority priority, T event) {
        return NeoForge.EVENT_BUS.post(priority, event);
    }

    static Path file(Path path) {
        if (Files.notExists(path) && FIRST_RUN) {
            try {
                Files.createFile(path);
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return path;
    }

    static Path dir(Path path) {
        if (Files.notExists(path) && FIRST_RUN) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return path;
    }
}
