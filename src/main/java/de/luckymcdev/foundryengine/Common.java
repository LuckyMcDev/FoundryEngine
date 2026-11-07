package de.luckymcdev.foundryengine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.area.common.AreaManager;
import de.luckymcdev.foundryengine.bundle.common.BundleManager;
import de.luckymcdev.foundryengine.bundle.common.BundleSavePathListener;
import de.luckymcdev.foundryengine.config.StartupConfig;
import de.luckymcdev.foundryengine.cutscene.common.CutsceneManager;
import de.luckymcdev.foundryengine.cutscene.common.CutsceneSessionManager;
import de.luckymcdev.foundryengine.dialogue.common.DialogueManager;
import de.luckymcdev.foundryengine.exception.common.UtilityClassException;
import de.luckymcdev.foundryengine.game.common.GameManager;
import de.luckymcdev.foundryengine.game.common.stage.GameStageHandler;
import de.luckymcdev.foundryengine.game.common.stage.table.StageTableManager;
import de.luckymcdev.foundryengine.network.common.NetworkManager;
import de.luckymcdev.foundryengine.registry.common.RegistryCollector;
import de.luckymcdev.foundryengine.savedata.common.SavedDataManager;
import de.luckymcdev.foundryengine.script.common.ScriptManager;
import de.luckymcdev.foundryengine.script.common.definition.ScriptEnv;
import de.luckymcdev.foundryengine.service.common.EngineServiceManager;
import de.luckymcdev.foundryengine.util.common.CompatibilityMode;
import de.luckymcdev.foundryengine.util.common.FirstRun;
import de.luckymcdev.foundryengine.waypoint.common.WaypointManager;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Shared constants and singleton managers for FoundryEngine.
 */
public final class Common {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "foundryengine";
	public static final String MODNAME = "FoundryEngine";
	public static final String BLOCK_DISPLAY_TAG = MODID + "_blockdisplay";
	public static final boolean FIRST_RUN = FirstRun.isFirstRun(MODID);
	private static final NetworkManager NETWORK_MANAGER = new NetworkManager();
	private static final SavedDataManager SAVED_DATA_MANAGER = new SavedDataManager(NETWORK_MANAGER);
	private static final GameStageHandler GAME_STAGE_HANDLER = new GameStageHandler();
	private static final StageTableManager STAGE_TABLE_MANAGER = new StageTableManager();
	private static final AreaManager AREA_MANAGER = new AreaManager(SAVED_DATA_MANAGER);
	private static final CutsceneManager CUTSCENE_MANAGER = new CutsceneManager(SAVED_DATA_MANAGER);
	private static final CutsceneSessionManager CUTSCENE_SESSION_MANAGER = new CutsceneSessionManager();
	private static final WaypointManager WAYPOINT_MANAGER = new WaypointManager(SAVED_DATA_MANAGER);
	private static final DialogueManager DIALOGUE_MANAGER = new DialogueManager(SAVED_DATA_MANAGER);
	private static final EngineServiceManager ENGINE_SERVICE_MANAGER = new EngineServiceManager();
	private static final ScriptManager SCRIPT_MANAGER = new ScriptManager(EnumSet.allOf(ScriptEnv.class));
	private static final BundleManager BUNDLE_MANAGER = new BundleManager(EngineMod.getModBus());
	private static final GameManager GAME_MANAGER = new GameManager();
	private static final List<Runnable> EVENT_CLEARERS = new ArrayList<>();
	private static @Nullable RegistryCollector registryCollector;

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
	 * Returns the singleton {@link ScriptManager}.
	 */
	public static ScriptManager getScriptManager() {
		return SCRIPT_MANAGER;
	}

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
	 * Returns the singleton {@link StageTableManager}.
	 */
	public static StageTableManager getStageTableManager() {
		return STAGE_TABLE_MANAGER;
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
	 * Returns the singleton {@link DialogueManager}.
	 */
	public static DialogueManager getDialogueManager() {
		return DIALOGUE_MANAGER;
	}

	/**
	 * Returns the singleton {@link EngineServiceManager}.
	 */
	public static EngineServiceManager getEngineServiceManager() {
		return ENGINE_SERVICE_MANAGER;
	}

	@Nullable
	public static RegistryCollector getRegistryCollector() {
		return registryCollector;
	}

	public static void setRegistryCollector(@Nullable RegistryCollector collector) {
		registryCollector = collector;
	}

	public static CompatibilityMode getCompatibilityMode() {
		return StartupConfig.COMPATIBILITY_MODE.get();
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
		registryCollector = null;
		for (var clearer : EVENT_CLEARERS) {
			clearer.run();
		}
	}
}