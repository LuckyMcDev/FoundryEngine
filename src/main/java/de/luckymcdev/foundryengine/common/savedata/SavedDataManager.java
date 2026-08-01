package de.luckymcdev.foundryengine.common.savedata;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.AreaManager;
import de.luckymcdev.foundryengine.common.cutscene.CutsceneManager;
import de.luckymcdev.foundryengine.common.dialogue.DialogueManager;
import de.luckymcdev.foundryengine.common.network.NetworkManager;
import de.luckymcdev.foundryengine.common.network.packets.sync.SavedDataSyncPacket;
import de.luckymcdev.foundryengine.common.waypoint.WaypointManager;
import de.luckymcdev.foundryengine.mixin.MinecraftServerAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Central persistence and sync for FoundryEngine's engine data.
 * <p>
 * Server-managed sections (areas, cutscenes, dialogue, waypoints) are persisted per world to
 * {@code <levelDir>/foundryengine/engine.dat} (resolved on {@code ServerStartedEvent}). On first
 * load of a world the legacy global file ({@link Common#ENGINE_DATA}) is imported once.
 * Client-local sections (e.g. the {@code hotkeys} section) stay in the legacy global file so they
 * survive across worlds and sessions; synced world data is kept in memory on the client.
 * <p>
 * Writes and broadcasts are deferred: mutations only mark the store dirty, and self-registered tick
 * listeners flush the disk at most every {@link #FLUSH_INTERVAL_TICKS} ticks and coalesce all
 * {@code syncToAll}/{@code syncToPlayer} calls into a single {@link SavedDataSyncPacket} per tick.
 */
public class SavedDataManager {
	public static final int FLUSH_INTERVAL_TICKS = 100;

	/**
	 * Sections owned by the server managers; never treated as client-local.
	 */
	private static final Set<String> SERVER_MANAGED_SECTIONS = Set.of(
		AreaManager.SAVE_SECTION,
		CutsceneManager.SAVE_SECTION,
		DialogueManager.SAVE_SECTION,
		WaypointManager.SAVE_SECTION
	);

	private final NetworkManager networkManager;
	private final Set<String> clientLocalSections = new HashSet<>();
	private CompoundTag data = new CompoundTag();
	private @Nullable Path diskFile;
	private boolean worldScoped = false;
	private boolean serverDirty = false;
	private boolean clientDirty = false;
	private boolean broadcastPending = false;
	private int tickCounter = 0;

	public SavedDataManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
		registerEvents();
	}

	private void registerEvents() {
		NeoForge.EVENT_BUS.addListener(this::onServerStarted);
		NeoForge.EVENT_BUS.addListener(this::onServerTick);
		NeoForge.EVENT_BUS.addListener(this::onServerStopping);
		if (FMLEnvironment.getDist().isClient()) {
			NeoForge.EVENT_BUS.addListener(this::onClientTick);
			NeoForge.EVENT_BUS.addListener(this::onClientLogout);
		}
	}

	/**
	 * Loads the engine data from disk. Uses the per-world file when a server has resolved it,
	 * otherwise falls back to the legacy global location until then.
	 */
	public void load() {
		CompoundTag loaded = new CompoundTag();
		Path file = resolvedFile();
		if (Files.exists(file)) {
			try (DataInputStream dis = new DataInputStream(Files.newInputStream(file))) {
				loaded = NbtIo.readCompressed(dis, NbtAccounter.defaultQuota());
			} catch (IOException e) {
				Common.LOGGER.error("Failed to load game data", e);
				loaded = new CompoundTag();
			}
		}
		preserveClientLocal(loaded);
		this.data = loaded;
	}

	/**
	 * Immediately flushes pending server and client data to disk (called on server stop).
	 */
	public void save() {
		flushServerData();
		if (clientDirty) {
			flushClientLocal();
		}
	}

	public CompoundTag getData() {
		return data;
	}

	/**
	 * Replaces the full engine data in memory. Client-local sections are preserved across the
	 * replacement so locally edited preferences survive server syncs.
	 */
	public void setData(CompoundTag tag) {
		CompoundTag merged = tag;
		if (!clientLocalSections.isEmpty()) {
			merged = tag.copy();
			preserveClientLocal(merged);
		}
		this.data = merged;
		clientDirty = true;
	}

	public CompoundTag getSection(String key) {
		return data.getCompound(key).orElseGet(CompoundTag::new);
	}

	/**
	 * Stores a section and marks the store dirty. Disk persistence is deferred: server-managed
	 * sections are flushed by the server tick listener, client-local sections by the client tick
	 * (or logout) listener.
	 */
	public void setSection(String key, CompoundTag section) {
		data.put(key, section);
		if (FMLEnvironment.getDist().isClient() && !SERVER_MANAGED_SECTIONS.contains(key)) {
			clientLocalSections.add(key);
			clientDirty = true;
		} else {
			serverDirty = true;
		}
	}

	/**
	 * Requests a broadcast of the full engine data. Coalesced: the next server tick sends a single
	 * {@link SavedDataSyncPacket} to all players if anything changed since the last send.
	 */
	public void syncToAll() {
		broadcastPending = true;
	}

	/**
	 * Requests a broadcast of the full engine data. Coalesced with {@link #syncToAll()}; the data
	 * is always a full snapshot so sending to all players satisfies the per-player request.
	 */
	public void syncToPlayer(ServerPlayer player) {
		broadcastPending = true;
	}

	private void onServerStarted(ServerStartedEvent event) {
		LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerAccess) event.getServer()).getStorageSource();
		Path worldFile = storageAccess.getLevelDirectory().path().resolve("foundryengine").resolve("engine.dat");
		migrateLegacyIfNeeded(worldFile);
		this.diskFile = worldFile;
		this.worldScoped = true;
		load();
		Common.getWaypointManager().load();
		Common.getAreaManager().load();
		Common.getCutsceneManager().load();
		Common.getDialogueManager().load();
		if (serverDirty) {
			flushServerData();
		}
		syncToAll();
	}

	private void onServerTick(ServerTickEvent.Post event) {
		tickCounter++;
		if (tickCounter >= FLUSH_INTERVAL_TICKS) {
			tickCounter = 0;
			if (serverDirty) {
				flushServerData();
			}
		}
		sendPendingBroadcast();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		save();
	}

	private void onClientTick(ClientTickEvent.Post event) {
		tickCounter++;
		if (tickCounter >= FLUSH_INTERVAL_TICKS) {
			tickCounter = 0;
			if (clientDirty) {
				flushClientLocal();
			}
		}
	}

	private void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		if (clientDirty) {
			flushClientLocal();
		}
	}

	private void sendPendingBroadcast() {
		if (!broadcastPending) {
			return;
		}
		broadcastPending = false;
		networkManager.sendToAllPlayers(new SavedDataSyncPacket(data.copy()));
	}

	private void flushServerData() {
		try {
			writeTag(resolvedFile(), data);
			serverDirty = false;
		} catch (IOException e) {
			Common.LOGGER.error("Failed to save game data", e);
		}
	}

	private void flushClientLocal() {
		try {
			writeClientLocal();
			clientDirty = false;
		} catch (IOException e) {
			Common.LOGGER.error("Failed to save client data", e);
		}
	}

	private void migrateLegacyIfNeeded(Path worldFile) {
		if (Files.exists(worldFile) || !Files.exists(Common.ENGINE_DATA)) {
			return;
		}
		try {
			CompoundTag legacy;
			try (DataInputStream dis = new DataInputStream(Files.newInputStream(Common.ENGINE_DATA))) {
				legacy = NbtIo.readCompressed(dis, NbtAccounter.defaultQuota());
			}
			writeTag(worldFile, legacy);
			Common.LOGGER.info("Imported legacy engine data into per-world save {}", worldFile);
		} catch (IOException e) {
			Common.LOGGER.error("Failed to migrate legacy engine data", e);
		}
	}

	/**
	 * Persists only the client-local sections, merged into the legacy global file so existing
	 * content (and the pre-refactor data) is preserved.
	 */
	private void writeClientLocal() throws IOException {
		if (clientLocalSections.isEmpty()) {
			return;
		}
		CompoundTag target = new CompoundTag();
		if (Files.exists(Common.ENGINE_DATA)) {
			try (DataInputStream dis = new DataInputStream(Files.newInputStream(Common.ENGINE_DATA))) {
				target = NbtIo.readCompressed(dis, NbtAccounter.defaultQuota());
			} catch (IOException e) {
				Common.LOGGER.warn("Failed to read existing client data; starting fresh", e);
				target = new CompoundTag();
			}
		}
		boolean changed = false;
		for (String key : clientLocalSections) {
			var section = data.getCompound(key);
			if (section.isPresent()) {
				target.put(key, section.get());
				changed = true;
			}
		}
		if (changed) {
			writeTag(Common.ENGINE_DATA, target);
		}
	}

	private void preserveClientLocal(CompoundTag target) {
		for (String key : clientLocalSections) {
			data.getCompound(key).ifPresent(section -> target.put(key, section));
		}
	}

	private void writeTag(Path file, CompoundTag tag) throws IOException {
		Files.createDirectories(file.getParent());
		try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(file))) {
			NbtIo.writeCompressed(tag, dos);
		}
	}

	private Path resolvedFile() {
		if (worldScoped && diskFile != null) {
			return diskFile;
		}
		return Common.ENGINE_DATA;
	}
}
