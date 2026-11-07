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
import de.luckymcdev.foundryengine.manager.api.ManagerRegistry;
import de.luckymcdev.foundryengine.manager.api.ManagerKey;
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

	private static final ManagerRegistry MANAGERS = new ManagerRegistry();

	// Foundational — must exist before any mod constructs.
	private static final List<Runnable> EVENT_CLEARERS = new ArrayList<>();
	private static @Nullable RegistryCollector registryCollector;

	static {
		var network = MANAGERS.register(ManagerKey.NETWORK, new NetworkManager());
		MANAGERS.register(ManagerKey.SAVED_DATA, new SavedDataManager(network));
		MANAGERS.register(ManagerKey.ENGINE_SERVICE, new EngineServiceManager());
		MANAGERS.register(ManagerKey.SCRIPT, new ScriptManager(EnumSet.allOf(ScriptEnv.class)));
		var bundle = MANAGERS.register(ManagerKey.BUNDLE, new BundleManager(EngineMod.getModBus()));

		bundle.getLifecycleDispatcher().register(new BundleSavePathListener());
		// GameManager's lifecycle registration moves to GameMod.
	}

	private Common() {
		throw new UtilityClassException();
	}

	public static ManagerRegistry getManagers() {
		return MANAGERS;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MODID, path);
	}

	public static Identifier mId(String path) {
		return Identifier.withDefaultNamespace(path);
	}

	public static Identifier id(String namespace, String path) {
		return Identifier.fromNamespaceAndPath(namespace, path);
	}

	// Foundational getters stay direct
	public static ScriptManager getScriptManager() {
		return MANAGERS.require(ManagerKey.SCRIPT, ScriptManager.class);
	}

	public static BundleManager getBundleManager() {
		return MANAGERS.require(ManagerKey.BUNDLE, BundleManager.class);
	}

	public static NetworkManager getNetworkManager() {
		return MANAGERS.require(ManagerKey.NETWORK, NetworkManager.class);
	}

	public static SavedDataManager getSavedDataManager() {
		return MANAGERS.require(ManagerKey.SAVED_DATA, SavedDataManager.class);
	}

	public static EngineServiceManager getEngineServiceManager() {
		return MANAGERS.require(ManagerKey.ENGINE_SERVICE, EngineServiceManager.class);
	}

	// Feature managers — delegate to the registry.
	public static GameStageHandler getGameStageHandler() {
		return MANAGERS.require(ManagerKey.GAME_STAGE, GameStageHandler.class);
	}

	public static StageTableManager getStageTableManager() {
		return MANAGERS.require(ManagerKey.STAGE_TABLE, StageTableManager.class);
	}

	public static AreaManager getAreaManager() {
		return MANAGERS.require(ManagerKey.AREA, AreaManager.class);
	}

	public static CutsceneManager getCutsceneManager() {
		return MANAGERS.require(ManagerKey.CUTSCENE, CutsceneManager.class);
	}

	public static CutsceneSessionManager getCutsceneSessionManager() {
		return MANAGERS.require(ManagerKey.CUTSCENE_SESSION, CutsceneSessionManager.class);
	}

	public static WaypointManager getWaypointManager() {
		return MANAGERS.require(ManagerKey.WAYPOINT, WaypointManager.class);
	}

	public static DialogueManager getDialogueManager() {
		return MANAGERS.require(ManagerKey.DIALOGUE, DialogueManager.class);
	}

	public static GameManager getGameManager() {
		return MANAGERS.require(ManagerKey.GAME, GameManager.class);
	}

	@Nullable
	public static RegistryCollector getRegistryCollector() {
		return registryCollector;
	}

	public static void setRegistryCollector(@Nullable RegistryCollector c) {
		registryCollector = c;
	}

	public static CompatibilityMode getCompatibilityMode() {
		return StartupConfig.COMPATIBILITY_MODE.get();
	}

	public static <T extends Event> T post(T event) {
		return NeoForge.EVENT_BUS.post(event);
	}

	public static <T extends Event> T post(EventPriority p, T event) {
		return NeoForge.EVENT_BUS.post(p, event);
	}

	public static void registerEventClear(Runnable clearer) {
		EVENT_CLEARERS.add(clearer);
	}

	public static void clearEvents() {
		registryCollector = null;
		for (var c : EVENT_CLEARERS) {
			c.run();
		}
	}
}