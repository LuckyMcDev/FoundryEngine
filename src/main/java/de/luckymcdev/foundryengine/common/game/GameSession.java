package de.luckymcdev.foundryengine.common.game;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A single game session with configurable tick and lifecycle handlers.
 */
public class GameSession {
	final GameData data;
	private final Identifier id;
	private boolean started;
	private GameState publicState = SimpleState.LOBBY;
	private boolean autoStart;
	private @Nullable Path worldDataPath;

	private Consumer<Level> commonTickHandler = level -> {
	};
	private BiConsumer<Minecraft, ClientLevel> clientTickHandler = (mc, level) -> {
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
	public void onClientTick(Minecraft client, Level level) {
		clientTickHandler.accept(client, (ClientLevel) level);
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
	public GameSession onClientTick(BiConsumer<Minecraft, ClientLevel> handler) {
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
	 * Loads the session data from persistent storage.
	 * Uses the world data path previously set via {@link #load(Path)}/{@link #save(Path)},
	 * falling back to {@link Common#GAME} (the shared cache dir) only when no world path
	 * has been provided — that fallback exists purely to keep direct script-driven calls
	 * working and should be avoided for world-scoped data.
	 */
	public void load() {
		data.loadFrom(worldDataPath != null ? worldDataPath : Common.GAME);
		flushInit();
	}

	/**
	 * Loads the session data from a world-specific directory.
	 */
	public void load(Path dataDir) {
		this.worldDataPath = dataDir;
		data.loadFrom(dataDir);
		flushInit();
	}

	private void flushInit() {
		if (data.isInitialized()) {
			onInit();
		}
	}

	/**
	 * Saves the session data to persistent storage.
	 * See {@link #load()} for the world-path vs. cache-dir fallback semantics.
	 */
	public void save() {
		data.saveTo(worldDataPath != null ? worldDataPath : Common.GAME);
	}

	/**
	 * Saves the session data to a world-specific directory.
	 */
	public void save(Path dataDir) {
		this.worldDataPath = dataDir;
		data.saveTo(dataDir);
	}

}
