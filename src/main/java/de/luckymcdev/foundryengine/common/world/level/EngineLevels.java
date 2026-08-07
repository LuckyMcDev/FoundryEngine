package de.luckymcdev.foundryengine.common.world.level;

import com.google.common.base.Preconditions;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevel;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelConfig;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelHandle;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelManager;
import de.luckymcdev.foundryengine.mixin.MinecraftServerAccess;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @see EngineLevels#get(MinecraftServer)
 * @see EngineLevels#openTemporaryLevel(RuntimeLevelConfig)
 * @see EngineLevels#getOrOpenPersistentLevel(Identifier, RuntimeLevelConfig)
 */
public final class EngineLevels {
	public static final Logger LOGGER = LogManager.getLogger(EngineLevels.class);
	public static final ResourceKey<DimensionType> DEFAULT_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, Common.id("default"));
	private static EngineLevels instance;

	static {
		NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> {
			EngineLevels levels = get(event.getServer());
			levels.tick();
		});

		NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> {
			EngineLevels levels = get(event.getServer());
			levels.onServerStopping();
			// Guard the static singleton against stale references to the stopping server:
			// only drop it if it still points at the instance we just shut down.
			if (instance == levels) {
				instance = null;
			}
		});
	}

	private final MinecraftServer server;
	private final MinecraftServerAccess serverAccess;
	private final RuntimeLevelManager levelManager;
	private final Set<ServerLevel> deletionQueue = new ReferenceOpenHashSet<>();
	private final Set<ServerLevel> unloadingQueue = new ReferenceOpenHashSet<>();

	private EngineLevels(MinecraftServer server) {
		this.server = server;
		this.serverAccess = (MinecraftServerAccess) server;

		this.levelManager = new RuntimeLevelManager(server);
	}

	/**
	 * Gets the  instance for the given server instance.
	 *
	 * @param server the server to work with
	 * @return the  instance to work with runtime dimensions
	 */
	public static EngineLevels get(MinecraftServer server) {
		Preconditions.checkState(server.isSameThread(), "cannot create levels from off-thread!");

		if (instance == null || instance.server != server) {
			instance = new EngineLevels(server);
		}

		return instance;
	}

	private static Identifier generateTemporaryLevelKey() {
		String key = RandomStringUtils.insecure().next(16, "abcdefghijklmnopqrstuvwxyz0123456789");
		return Common.id(key);
	}

	private void tick() {
		Set<ServerLevel> deletionQueue = this.deletionQueue;
		if (!deletionQueue.isEmpty()) {
			deletionQueue.removeIf(this::tickDeleteLevel);
		}

		Set<ServerLevel> unloadingQueue = this.unloadingQueue;
		if (!unloadingQueue.isEmpty()) {
			unloadingQueue.removeIf(this::tickUnloadLevel);
		}
	}

	/**
	 * Creates a new temporary level with the given {@link RuntimeLevelConfig} that will not be saved and will be
	 * deleted when the server exits.
	 * <p>
	 * The created level is returned asynchronously through a {@link RuntimeLevelHandle}.
	 * This handle can be used to acquire the {@link ServerLevel} object through {@link RuntimeLevelHandle#asLevel()},
	 * as well as to delete the level through {@link RuntimeLevelHandle#delete()}.
	 *
	 * @param config the config with which to construct this temporary level
	 * @return a future providing the created level
	 */
	public RuntimeLevelHandle openTemporaryLevel(RuntimeLevelConfig config) {
		return this.openTemporaryLevel(generateTemporaryLevelKey(), config);
	}

	/**
	 * Creates a new temporary level with the given identifier and {@link RuntimeLevelConfig} that will not be saved and will be
	 * deleted when the server exits.
	 * <p>
	 * The created level is returned asynchronously through a {@link RuntimeLevelHandle}.
	 * This handle can be used to acquire the {@link ServerLevel} object through {@link RuntimeLevelHandle#asLevel()},
	 * as well as to delete the level through {@link RuntimeLevelHandle#delete()}.
	 *
	 * @param key    the unique identifier for this dimension
	 * @param config the config with which to construct this temporary level
	 * @return a future providing the created level
	 */
	public RuntimeLevelHandle openTemporaryLevel(Identifier key, RuntimeLevelConfig config) {
		RuntimeLevel level = this.addTemporaryLevel(key, config);
		return new RuntimeLevelHandle(this, level);
	}

	/**
	 * Gets or creates a new persistent level with the given identifier and {@link RuntimeLevelConfig}. These levels
	 * will be saved to disk and can be restored after a server restart.
	 * <p>
	 * If a level with this identifier exists already, it will be returned and no new level will be constructed.
	 * <p>
	 * <b>Note!</b> These persistent levels will not be automatically restored! This function
	 * must be called after a server restart with the relevant identifier and configuration such that it can be loaded.
	 * <p>
	 * The created level is returned asynchronously through a {@link RuntimeLevelHandle}.
	 * This handle can be used to acquire the {@link ServerLevel} object through {@link RuntimeLevelHandle#asLevel()},
	 * as well as to delete the level through {@link RuntimeLevelHandle#delete()}.
	 *
	 * @param key    the unique identifier for this dimension
	 * @param config the config with which to construct this persistent level
	 * @return a future providing the created level
	 */
	public RuntimeLevelHandle getOrOpenPersistentLevel(Identifier key, RuntimeLevelConfig config) {
		ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, key);

		ServerLevel level = this.server.getLevel(levelKey);
		if (level == null) {
			level = this.addPersistentLevel(key, config);
		} else {
			this.deletionQueue.remove(level);
			this.unloadingQueue.remove(level);
		}

		return new RuntimeLevelHandle(this, level);
	}

	private RuntimeLevel addPersistentLevel(Identifier key, RuntimeLevelConfig config) {
		ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, key);
		return this.levelManager.add(levelKey, config, RuntimeLevel.Style.PERSISTENT);
	}

	private RuntimeLevel addTemporaryLevel(Identifier key, RuntimeLevelConfig config) {
		ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, key);

		try {
			LevelStorageSource.LevelStorageAccess session = this.serverAccess.getStorageSource();
			FileUtils.forceDeleteOnExit(session.getDimensionPath(levelKey).toFile());
		} catch (IOException ignored) {
		}

		return this.levelManager.add(levelKey, config, RuntimeLevel.Style.TEMPORARY);
	}

	public void enqueueLevelDeletion(ServerLevel level) {
		this.server.execute(() -> {
			level.getChunkSource().deactivateTicketsOnClosing();
			level.noSave = true;
			this.kickPlayers(level);
			this.deletionQueue.add(level);
		});
	}

	public void enqueueLevelUnloading(ServerLevel level) {
		this.server.execute(() -> {
			level.noSave = false;
			level.getChunkSource().deactivateTicketsOnClosing();
			level.getChunkSource().tick(() -> true, false);
			this.kickPlayers(level);
			this.unloadingQueue.add(level);
		});
	}

	public boolean tickDeleteLevel(ServerLevel level) {
		//if (this.isLevelActive(level)) {
		this.kickPlayers(level);
		this.levelManager.delete(level);
		return true;
		//} else {
		//    this.kickPlayers(level);
		//    return false;
		//}
	}

	public boolean tickUnloadLevel(ServerLevel level) {
		if (this.isLevelActive(level) && !level.getChunkSource().chunkMap.hasWork()) {
			this.levelManager.unload(level);
			return true;
		} else {
			this.kickPlayers(level);
			return false;
		}
	}

	private void kickPlayers(ServerLevel level) {
		if (level.players().isEmpty()) {
			return;
		}

		ServerLevel spawnLevel = this.server.findRespawnDimension();
		LevelData.RespawnData spawnPoint = this.server.getRespawnData();

		List<ServerPlayer> players = new ArrayList<>(level.players());

		for (ServerPlayer player : players) {
			//? if 26.1 {
			/*Vec3 pos = player.adjustSpawnLocation(spawnLevel, spawnPoint.pos()).getBottomCenter();
			 *///?} else {
			Vec3 pos = Vec3.atBottomCenterOf(player.adjustSpawnLocation(spawnLevel, spawnPoint.pos()));
			//?}
			TeleportTransition target = new TeleportTransition(spawnLevel, pos, Vec3.ZERO, spawnPoint.yaw(), spawnPoint.pitch(), TeleportTransition.DO_NOTHING);

			player.teleport(target);
		}
	}

	private boolean isLevelActive(ServerLevel level) {
		return level.players().isEmpty() && level.getChunkSource().getLoadedChunksCount() <= 0;
	}

	private void onServerStopping() {
		// Drain pending deletions/unloads so persistent-level queues are not left behind in
		// the (now stale) singleton when the server exits.
		if (!deletionQueue.isEmpty()) {
			deletionQueue.forEach(this::tickDeleteLevel);
			deletionQueue.clear();
		}
		if (!unloadingQueue.isEmpty()) {
			unloadingQueue.forEach(this::tickUnloadLevel);
			unloadingQueue.clear();
		}

		List<RuntimeLevel> temporaryLevels = this.collectTemporaryLevels();
		for (RuntimeLevel temporary : temporaryLevels) {
			this.kickPlayers(temporary);
			this.levelManager.delete(temporary);
		}
	}

	private List<RuntimeLevel> collectTemporaryLevels() {
		List<RuntimeLevel> temporaryLevels = new ArrayList<>();
		for (ServerLevel level : this.server.getAllLevels()) {
			if (level instanceof RuntimeLevel runtimeLevel) {
				if (runtimeLevel.style == RuntimeLevel.Style.TEMPORARY) {
					temporaryLevels.add(runtimeLevel);
				}
			}
		}
		return temporaryLevels;
	}
}
