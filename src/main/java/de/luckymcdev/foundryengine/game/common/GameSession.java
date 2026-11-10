package de.luckymcdev.foundryengine.game.common;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.EnginePaths;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A single game session with configurable tick and lifecycle handlers.
 */
public class GameSession {
	private static final Logger LOGGER = LogUtils.getLogger();
	final GameData data;
	private final Identifier id;
	private boolean started;
	private GameState publicState = SimpleState.LOBBY;
	private boolean autoStart;
	private @Nullable String worldName;
	private @Nullable Path worldDataPath;

	private Consumer<Level> commonTickHandler = level -> {
	};
	private BiConsumer<Object, Level> clientTickHandler = (client, level) -> {
	};
	private BiConsumer<MinecraftServer, ServerLevel> serverTickHandler = (server, level) -> {
	};
	private Runnable startingHandler = () -> {
	};
	private Runnable stoppingHandler = () -> {
	};
	private Runnable initHandler = () -> {
	};

	public GameSession(Identifier id, GameData data) {
		this.id = id;
		this.data = data;
	}

	/**
	 * Returns the unique identifier for this session.
	 */
	public Identifier id() {
		return id;
	}

	/**
	 * Returns the persistent data for this session.
	 */
	public GameData data() {
		return data;
	}

	/**
	 * Returns the world this session is currently bound to, or {@code null} if it has
	 * not been loaded for a world yet.
	 */
	public @Nullable String worldName() {
		return worldName;
	}

	/**
	 * Returns the game-defined public state.
	 */
	public GameState publicState() {
		return publicState;
	}

	/**
	 * Returns true if the session has been started and this started, or false otherwise.
	 */
	boolean isStarted() {
		return started;
	}

	/**
	 * Marks the session as started or stopped. Owned by {@link GameManager}.
	 */
	void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * Sets the game-defined public state.
	 */
	public GameSession publicState(GameState publicState) {
		this.publicState = publicState;
		return this;
	}

	/**
	 * Returns true if this session should auto-start when the world loads.
	 */
	public boolean autoStart() {
		return autoStart;
	}

	/**
	 * Sets whether this session should auto-start when the world loads.
	 */
	public GameSession autoStart(boolean autoStart) {
		this.autoStart = autoStart;
		return this;
	}

	/**
	 * Called on every common tick for this session.
	 */
	public void onCommonTick(Level level) {
		commonTickHandler.accept(level);
	}

	/**
	 * Called on every client tick for this session.
	 * This cast is needed due to env separation
	 */
	public void onClientTick(Object client, Level level) {
		clientTickHandler.accept(client, level);
	}

	/**
	 * Called on every server tick for this session.
	 */
	public void onServerTick(MinecraftServer server, ServerLevel level) {
		serverTickHandler.accept(server, level);
	}

	/**
	 * Called when the session is starting.
	 */
	public void onStarting() {
		startingHandler.run();
	}

	/**
	 * Called when the session is stopping.
	 */
	public void onStopping() {
		stoppingHandler.run();
	}

	/**
	 * Called once when the session data is first initialized.
	 */
	public void onInit() {
		initHandler.run();
	}

	/**
	 * Sets the common tick handler for this session.
	 */
	public GameSession onCommonTick(Consumer<Level> handler) {
		this.commonTickHandler = handler;
		return this;
	}

	/**
	 * Sets the client tick handler for this session.
	 */
	public GameSession onClientTick(BiConsumer<Object, Level> handler) {
		this.clientTickHandler = handler;
		return this;
	}

	/**
	 * Sets the server tick handler for this session.
	 */
	public GameSession onServerTick(BiConsumer<MinecraftServer, ServerLevel> handler) {
		this.serverTickHandler = handler;
		return this;
	}

	/**
	 * Sets the starting handler for this session.
	 */
	public GameSession onStarting(Runnable handler) {
		this.startingHandler = handler;
		return this;
	}

	/**
	 * Sets the stopping handler for this session.
	 */
	public GameSession onStopping(Runnable handler) {
		this.stoppingHandler = handler;
		return this;
	}

	/**
	 * Sets the one-time initialization handler for this session.
	 */
	public GameSession onInit(Runnable handler) {
		this.initHandler = handler;
		return this;
	}

	/**
	 * Loads the session data for the given world from persistent storage.
	 * This binds the session's {@link GameData} to that world's per-world tag store.
	 */
	public void load(String worldName, Path dataDir) {
		this.worldName = worldName;
		this.worldDataPath = dataDir;
		data.bindWorld(worldName);
		data.loadFrom(dataDir);
		flushInit();
	}

	/**
	 * Reloads the session data for the world this session was last bound to.
	 * Does nothing (and logs a warning) if the session has never been bound.
	 */
	public void load() {
		if (worldName == null || worldDataPath == null) {
			LOGGER.warn("Cannot load game session [{}]: no world bound", id);
			return;
		}
		load(worldName, worldDataPath);
	}

	private void flushInit() {
		if (data.isInitialized()) {
			onInit();
		}
	}

	/**
	 * Saves the session data for the given world to persistent storage.
	 */
	public void save(String worldName, Path dataDir) {
		this.worldName = worldName;
		this.worldDataPath = dataDir;
		data.bindWorld(worldName);
		data.saveTo(dataDir);
	}

	/**
	 * Saves the session data for the world this session was last bound to.
	 * Does nothing (and logs a warning) if the session has never been bound.
	 */
	public void save() {
		if (worldName == null || worldDataPath == null) {
			LOGGER.warn("Cannot save game session [{}]: no world bound", id);
			return;
		}
		save(worldName, worldDataPath);
	}
}