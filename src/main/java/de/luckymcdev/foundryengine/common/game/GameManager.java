package de.luckymcdev.foundryengine.common.game;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.BundleLifecycleListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle and tick dispatch of game sessions, scoped per world.
 * <p>
 * Lifecycle tracking (STOPPED → STARTING → RUNNING → STOPPING → STOPPED) is per-world,
 * not per-session. All sessions in a world share the world's lifecycle.
 */
public class GameManager implements BundleLifecycleListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, Map<Identifier, GameSession>> worlds = new ConcurrentHashMap<>();
	private final Map<String, Path> worldDataPaths = new ConcurrentHashMap<>();
	private final Map<String, GameLifecycle> worldLifecycles = new ConcurrentHashMap<>();
	private final Map<Identifier, GameSession> globalSessions = new ConcurrentHashMap<>();
	private final Map<Identifier, CompoundTag> persistentSessionData = new ConcurrentHashMap<>();
	private final Set<GameSession> tickedCommonSessions = ConcurrentHashMap.newKeySet();
	private final Set<GameSession> tickedServerSessions = ConcurrentHashMap.newKeySet();

	private static String worldName(Level level) {
		if (level == null) {
			return "unknown";
		}
		if (level.getServer() != null) {
			return level.getServer().getWorldData().getLevelName();
		}
		Minecraft client = Minecraft.getInstance();
		if (client.getSingleplayerServer() != null) {
			return client.getSingleplayerServer().getWorldData().getLevelName();
		}
		// Pure client (dedicated-server multiplayer): resolve a meaningful name from the
		// connected server's data instead of falling back to "unknown".
		ServerData currentServer = client.getCurrentServer();
		if (currentServer != null && currentServer.name != null && !currentServer.name.isBlank()) {
			return currentServer.name;
		}
		return "unknown";
	}

	/**
	 * Resets the per-tick session deduplication state. Must be called once per server tick
	 * before any level/session ticking so that each session's tick handlers run exactly once
	 * per server tick regardless of how many dimensions the world has.
	 */
	public void beginServerTick() {
		tickedCommonSessions.clear();
		tickedServerSessions.clear();
	}

	/**
	 * Registers a game session for all worlds (current and future).
	 * Returns false if a session with the same ID is already registered globally.
	 */
	public boolean register(GameSession session) {
		Identifier id = session.id();
		if (globalSessions.putIfAbsent(id, session) != null) {
			LOGGER.warn("Game session [{}] is already registered globally", id);
			return false;
		}
		for (Map<Identifier, GameSession> worldSessions : worlds.values()) {
			worldSessions.putIfAbsent(id, session);
		}
		LOGGER.debug("Registered game session [{}] globally", id);
		return true;
	}

	/**
	 * Registers a game session for the given world. Returns false if already registered.
	 */
	public boolean register(String worldName, GameSession session) {
		Identifier id = session.id();
		Map<Identifier, GameSession> sessions = worlds(worldName);
		if (sessions.putIfAbsent(id, session) != null) {
			LOGGER.warn("Game session [{}] is already registered for world [{}]", id, worldName);
			return false;
		}
		LOGGER.debug("Registered game session [{}] for world [{}]", id, worldName);
		return true;
	}

	/**
	 * Unregisters a game session for the given world and stops it if started.
	 */
	public void unregister(String worldName, Identifier id) {
		Map<Identifier, GameSession> sessions = worlds.get(worldName);
		if (sessions == null) {
			return;
		}
		GameSession session = sessions.remove(id);
		if (session != null && session.started) {
			stopSession(worldName, id);
		}
		LOGGER.debug("Unregistered game session [{}] from world [{}]", id, worldName);
	}

	/**
	 * Returns the registered session for the given world, or null if not found.
	 */
	public @Nullable GameSession getSession(String worldName, Identifier id) {
		Map<Identifier, GameSession> sessions = worlds.get(worldName);
		if (sessions != null) {
			GameSession session = sessions.get(id);
			if (session != null) {
				return session;
			}
		}
		return globalSessions.get(id);
	}

	/**
	 * Returns an unmodifiable collection of all sessions for the given world.
	 */
	public Collection<GameSession> getSessions(String worldName) {
		Map<Identifier, GameSession> sessions = worlds.get(worldName);
		if (sessions == null) {
			return List.of();
		}
		return Collections.unmodifiableCollection(sessions.values());
	}

	/**
	 * Returns all sessions across all worlds and global sessions.
	 */
	public Collection<GameSession> getAllSessions() {
		List<GameSession> all = new ArrayList<>(globalSessions.values());
		for (Map<Identifier, GameSession> s : worlds.values()) {
			for (GameSession session : s.values()) {
				if (!all.contains(session)) {
					all.add(session);
				}
			}
		}
		return Collections.unmodifiableCollection(all);
	}

	/**
	 * Checks if a session with the given ID is registered for the given world.
	 */
	public boolean hasSession(String worldName, Identifier id) {
		Map<Identifier, GameSession> sessions = worlds.get(worldName);
		if (sessions != null && sessions.containsKey(id)) {
			return true;
		}
		return globalSessions.containsKey(id);
	}

	public boolean anySession() {
		return !globalSessions.isEmpty() || !worlds.isEmpty();
	}

	/**
	 * Returns the lifecycle state for the given world.
	 */
	public GameLifecycle worldLifecycle(String worldName) {
		return worldLifecycles.getOrDefault(worldName, GameLifecycle.STOPPED);
	}

	/**
	 * Returns the persistent CompoundTag for a session ID, creating one if absent.
	 * The returned tag survives bundle reloads and is shared by all GameData instances
	 * with the same ID.
	 */
	public CompoundTag getOrCreateSessionData(Identifier id) {
		return persistentSessionData.computeIfAbsent(id, k -> new CompoundTag());
	}

	/**
	 * Sets the data directory for a world, used for session persistence.
	 */
	public void setWorldDataPath(String worldName, Path dataPath) {
		worldDataPaths.put(worldName, dataPath);
	}

	/**
	 * Returns the data directory for a world, or {@link Common#GAME} if not set.
	 */
	public Path worldDataPath(String worldName) {
		return worldDataPaths.getOrDefault(worldName, Common.GAME);
	}

	/**
	 * Starts all auto-start sessions for the given world and sets its lifecycle to RUNNING.
	 */
	public void autoStartAll(String worldName) {
		if (worldLifecycle(worldName) != GameLifecycle.STOPPED) {
			return;
		}
		Path dataPath = worldDataPath(worldName);
		Map<Identifier, GameSession> sessions = worlds(worldName);
		if (sessions.isEmpty()) {
			return;
		}

		worldLifecycles.put(worldName, GameLifecycle.STARTING);
		for (GameSession session : sessions.values()) {
			if (session.autoStart() && !session.started) {
				startSession(worldName, session.id(), dataPath);
			}
		}
		worldLifecycles.put(worldName, GameLifecycle.RUNNING);
		LOGGER.debug("World [{}] lifecycle set to RUNNING with {} sessions", worldName, sessions.size());
	}

	/**
	 * Starts the given session for the given world.
	 */
	public boolean startSession(String worldName, Identifier id) {
		return startSession(worldName, id, worldDataPath(worldName));
	}

	private boolean startSession(String worldName, Identifier id, Path dataPath) {
		GameSession session = getSession(worldName, id);
		if (session == null) {
			LOGGER.warn("Cannot start unknown session [{}] for world [{}]", id, worldName);
			return false;
		}
		if (session.started) {
			return false;
		}

		GameSessionEvent.Starting startingEvent = new GameSessionEvent.Starting(id);
		if (NeoForge.EVENT_BUS.post(startingEvent).isCanceled()) {
			return false;
		}

		session.load(dataPath);
		session.onStarting();
		session.started = true;

		NeoForge.EVENT_BUS.post(new GameSessionEvent.Started(id));
		LOGGER.debug("Started game session [{}] for world [{}]", id, worldName);
		return true;
	}

	/**
	 * Stops the given session for the given world.
	 */
	public boolean stopSession(String worldName, Identifier id) {
		GameSession session = getSession(worldName, id);
		if (session == null) {
			return false;
		}
		if (!session.started) {
			return false;
		}

		GameSessionEvent.Stopping stoppingEvent = new GameSessionEvent.Stopping(id);
		if (NeoForge.EVENT_BUS.post(stoppingEvent).isCanceled()) {
			return false;
		}

		Path dataPath = worldDataPath(worldName);
		try {
			session.onStopping();
			session.save(dataPath);
		} finally {
			session.started = false;
		}

		NeoForge.EVENT_BUS.post(new GameSessionEvent.Stopped(id));
		LOGGER.debug("Stopped game session [{}] for world [{}]", id, worldName);
		return true;
	}

	/**
	 * Stops all sessions for the given world and sets its lifecycle to STOPPED.
	 */
	public void stopAll(String worldName) {
		Map<Identifier, GameSession> sessions = worlds.get(worldName);
		if (sessions == null) {
			return;
		}
		for (Identifier id : sessions.keySet()) {
			stopSession(worldName, id);
		}
		worldLifecycles.put(worldName, GameLifecycle.STOPPED);
	}

	/**
	 * Stops all sessions across all worlds.
	 */
	public void stopAll() {
		for (String worldName : worlds.keySet()) {
			stopAll(worldName);
		}
	}

	/**
	 * Ticks all started sessions for the world of the given level.
	 */
	public void tickCommon(Level level) {
		String name = worldName(level);
		if (worldLifecycle(name) != GameLifecycle.RUNNING) {
			return;
		}
		Map<Identifier, GameSession> sessions = worlds.get(name);
		if (sessions == null || sessions.isEmpty()) {
			return;
		}

		for (GameSession session : sessions.values()) {
			if (session.started && session.publicState().isActive()
				&& tickedCommonSessions.add(session)) {
				session.onCommonTick(level);
			}
		}
	}

	/**
	 * Ticks all started sessions for the world of the given client level.
	 */
	public void tickClient(Minecraft client, Level level) {
		String name = worldName(level);
		if (worldLifecycle(name) != GameLifecycle.RUNNING) {
			return;
		}
		Map<Identifier, GameSession> sessions = worlds.get(name);
		if (sessions == null) {
			return;
		}

		for (GameSession session : sessions.values()) {
			if (session.started && session.publicState().isActive()) {
				session.onClientTick(client, level);
			}
		}
	}

	/**
	 * Ticks all started sessions for the world of the given server level.
	 */
	public void tickServer(MinecraftServer server, ServerLevel level) {
		String name = worldName(level);
		if (worldLifecycle(name) != GameLifecycle.RUNNING) {
			return;
		}
		Map<Identifier, GameSession> sessions = worlds.get(name);
		if (sessions == null) {
			return;
		}

		for (GameSession session : sessions.values()) {
			if (session.started && session.publicState().isActive()
				&& tickedServerSessions.add(session)) {
				session.onServerTick(server, level);
			}
		}
	}

	@Override
	public void onBundlePreUnload(Bundle bundle) {
		for (Map.Entry<String, Map<Identifier, GameSession>> world : worlds.entrySet()) {
			String worldName = world.getKey();
			for (Identifier id : world.getValue().keySet()) {
				if (id.getNamespace().equals(bundle.info().id())) {
					stopSession(worldName, id);
				}
			}
		}
	}

	@Override
	public void onBundleReloadStarted() {
		stopAll();
		worlds.clear();
		worldLifecycles.clear();
		globalSessions.clear();
	}

	@Override
	public void onBundleReloadCompleted() {
		for (String worldName : worldDataPaths.keySet()) {
			autoStartAll(worldName);
		}
	}

	private Map<Identifier, GameSession> worlds(String world) {
		return worlds.computeIfAbsent(world, k -> new ConcurrentHashMap<>(globalSessions));
	}
}
