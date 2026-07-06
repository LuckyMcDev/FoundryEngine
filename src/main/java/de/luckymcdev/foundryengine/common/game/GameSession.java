package de.luckymcdev.foundryengine.common.game;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A single game session with configurable tick and lifecycle handlers.
 */
public class GameSession {
	final GameData data;
	private final Identifier id;
	private GameState state = GameState.STOPPED;

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
	 * Returns the current lifecycle state of this session.
	 */
	public GameState state() {
		return state;
	}

	void state(GameState state) {
		this.state = state;
	}

	/**
	 * Called on every common tick for this session.
	 */
	public void onCommonTick(Level level) {
		commonTickHandler.accept(level);
	}

	/**
	 * Called on every client tick for this session.
	 */
	public void onClientTick(Minecraft client, ClientLevel level) {
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
	 * Loads the session data from persistent storage.
	 */
	public void load() {
		data.loadFrom(Common.GAME);
	}

	/**
	 * Saves the session data to persistent storage.
	 */
	public void save() {
		data.saveTo(Common.GAME);
	}

	/**
	 * Returns true if the session is currently running.
	 */
	public boolean isRunning() {
		return state.isRunning();
	}
}
