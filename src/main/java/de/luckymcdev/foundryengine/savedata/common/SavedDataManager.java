package de.luckymcdev.foundryengine.savedata.common;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.EnginePaths;
import de.luckymcdev.foundryengine.area.common.AreaManager;
import de.luckymcdev.foundryengine.cutscene.common.CutsceneManager;
import de.luckymcdev.foundryengine.dialogue.common.DialogueManager;
import de.luckymcdev.foundryengine.network.common.NetworkManager;
import de.luckymcdev.foundryengine.network.common.packet.sync.SavedDataSyncPacket;
import de.luckymcdev.foundryengine.waypoint.common.WaypointManager;
import de.luckymcdev.foundryengine.mixin.MinecraftServerAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/// Central persistence and sync for FoundryEngine's engine data, stored as one `<sectionKey>.dat` file
/// per section in either the per-world ([Scope#WORLD]) or the shared ([Scope#GLOBAL]) directory.
/// Writes and syncs are deferred to tick listeners.
public class SavedDataManager {
	public static final int FLUSH_INTERVAL_TICKS = 100;

	private static final String FILE_EXTENSION = ".dat";
	private static final String WORLD_DIR_NAME = "foundryengine";
	/// Sections owned by the server managers; always [Scope#WORLD].
	private static final Set<String> SERVER_MANAGED_SECTIONS = Set.of(
		AreaManager.SAVE_SECTION,
		CutsceneManager.SAVE_SECTION,
		DialogueManager.SAVE_SECTION,
		WaypointManager.SAVE_SECTION
	);
	private final Map<String, Scope> sectionScopes = new ConcurrentHashMap<>();

	private final NetworkManager networkManager;
	private final Set<String> dirtySections = ConcurrentHashMap.newKeySet();
	private final Path dataDir = EnginePaths.ENGINE_DATA.getParent().resolve("data");
	private @Nullable Path worldDir;
	private CompoundTag data = new CompoundTag();
	private int serverTickCounter = 0;
	private boolean broadcastPending = false;
	private int clientTickCounter = 0;
	public SavedDataManager(NetworkManager networkManager) {
		this.networkManager = networkManager;
		for (String key : SERVER_MANAGED_SECTIONS) {
			sectionScopes.put(key, Scope.WORLD);
		}
		registerEvents();
	}

	private static String fileNameFor(String key) {
		return key + FILE_EXTENSION;
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

	private static String keyOf(Path file) {
		String name = file.getFileName().toString();
		return name.substring(0, name.length() - FILE_EXTENSION.length());
	}

	private static boolean isSectionFile(Path file) {
		String name = file.getFileName().toString();
		return name.endsWith(FILE_EXTENSION) && Files.isRegularFile(file);
	}

	/// Declares in which [Scope] a section is stored; unregistered sections default to [Scope#GLOBAL].
	/// Call this before the section is first loaded or set.
	public void registerSection(String key, Scope scope) {
		sectionScopes.put(key, scope);
	}

	public Scope scopeOf(String key) {
		return sectionScopes.getOrDefault(key, Scope.GLOBAL);
	}

	public CompoundTag getData() {
		return data;
	}

	/// Loads the global sections, and the world sections too if a world is resolved, discarding unsaved
	/// changes of those scopes.
	public void load() {
		loadScope(Scope.GLOBAL);
		loadScope(Scope.WORLD);
	}

	public CompoundTag getSection(String key) {
		return data.getCompound(key).orElseGet(CompoundTag::new);
	}

	/// Immediately flushes all dirty sections to disk.
	public void save() {
		flushDirty(Scope.WORLD);
		flushDirty(Scope.GLOBAL);
	}

	/// Replaces the in-memory data with a server sync while keeping the global sections.
	/// Nothing is marked dirty, so synced world data is never persisted by the receiving client.
	public void setData(CompoundTag tag) {
		CompoundTag merged = tag.copy();
		for (String key : List.copyOf(data.keySet())) {
			if (scopeOf(key) == Scope.GLOBAL) {
				data.getCompound(key).ifPresent(section -> merged.put(key, section));
			}
		}
		this.data = merged;
	}

	/// Stores a section and marks it dirty; the actual write is deferred to the tick listeners.
	public void setSection(String key, CompoundTag section) {
		data.put(key, section);
		dirtySections.add(key);
	}

	/// Requests a broadcast of all [Scope#WORLD] sections to all players on the next server tick.
	/// There is no per-player variant, since any targeted request is a subset of this broadcast.
	public void syncToAll() {
		broadcastPending = true;
	}

	private void onServerStarted(ServerStartedEvent event) {
		LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerAccess) event.getServer()).getStorageSource();
		this.worldDir = storageAccess.getLevelDirectory().path().resolve(WORLD_DIR_NAME);
		loadScope(Scope.WORLD);
		Common.getWaypointManager().load();
		Common.getAreaManager().load();
		Common.getCutsceneManager().load();
		Common.getDialogueManager().load();
		flushDirty(Scope.WORLD);
		syncToAll();
	}

	private void onServerTick(ServerTickEvent.Post event) {
		serverTickCounter++;
		if (serverTickCounter >= FLUSH_INTERVAL_TICKS) {
			serverTickCounter = 0;
			flushDirty(Scope.WORLD);
			if (!FMLEnvironment.getDist().isClient()) {
				// A dedicated server has no client tick, so it flushes global sections itself.
				flushDirty(Scope.GLOBAL);
			}
		}
		sendPendingBroadcast();
	}

	private void onServerStopping(ServerStoppingEvent event) {
		save();
		// Stop persisting world sections so they can never leak into another world.
		this.worldDir = null;
	}

	private void onClientTick(ClientTickEvent.Post event) {
		clientTickCounter++;
		if (clientTickCounter >= FLUSH_INTERVAL_TICKS) {
			clientTickCounter = 0;
			flushDirty(Scope.GLOBAL);
		}
	}

	private void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		flushDirty(Scope.GLOBAL);
	}

	private void sendPendingBroadcast() {
		if (!broadcastPending) {
			return;
		}
		broadcastPending = false;
		networkManager.sendToAllPlayers(new SavedDataSyncPacket(worldSnapshot()));
	}

	private CompoundTag worldSnapshot() {
		CompoundTag snapshot = new CompoundTag();
		for (String key : List.copyOf(data.keySet())) {
			if (scopeOf(key) == Scope.WORLD) {
				data.getCompound(key).ifPresent(section -> snapshot.put(key, section.copy()));
			}
		}
		return snapshot;
	}

	private @Nullable Path dirFor(Scope scope) {
		return scope == Scope.WORLD ? worldDir : dataDir;
	}

	/// Writes every dirty section of the scope, doing nothing while the scope has no directory yet.
	/// A failed write leaves the section dirty so the next flush retries it.
	private void flushDirty(Scope scope) {
		Path dir = dirFor(scope);
		if (dir == null) {
			return;
		}
		for (String key : List.copyOf(dirtySections)) {
			if (scopeOf(key) != scope || !dirtySections.remove(key)) {
				continue;
			}
			try {
				writeTag(dir.resolve(fileNameFor(key)), getSection(key));
			} catch (IOException e) {
				dirtySections.add(key);
				Common.LOGGER.error("Failed to save section '{}' to {}", key, dir, e);
			}
		}
	}

	/// Replaces all in-memory sections of the scope with the ones on disk.
	private void loadScope(Scope scope) {
		Path dir = dirFor(scope);
		if (dir == null) {
			return;
		}

		for (String key : List.copyOf(data.keySet())) {
			if (scopeOf(key) == scope) {
				data.remove(key);
			}
		}
		dirtySections.removeIf(key -> scopeOf(key) == scope);

		if (!Files.isDirectory(dir)) {
			return;
		}
		try (Stream<Path> files = Files.list(dir)) {
			files.filter(SavedDataManager::isSectionFile).forEach(file -> {
				String key = keyOf(file);
				if (scopeOf(key) != scope) {
					return;
				}
				CompoundTag section = readTag(file);
				if (section != null) {
					data.put(key, section);
				}
			});
		} catch (IOException e) {
			Common.LOGGER.error("Failed to list engine data in {}", dir, e);
		}
	}

	private @Nullable CompoundTag readTag(Path file) {
		try (DataInputStream dis = new DataInputStream(Files.newInputStream(file))) {
			return NbtIo.readCompressed(dis, NbtAccounter.defaultQuota());
		} catch (IOException e) {
			Common.LOGGER.error("Failed to load {}", file, e);
			return null;
		}
	}

	/// Writes to a temp file and then moves it over the target, so a crash mid-write cannot corrupt it.
	private void writeTag(Path file, CompoundTag tag) throws IOException {
		Files.createDirectories(file.getParent());
		Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
		try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(tmp))) {
			NbtIo.writeCompressed(tag, dos);
		}
		try {
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/// Where a section is persisted.
	public enum Scope {
		/// Shared by all worlds, stored in the FoundryEngine directory
		GLOBAL,
		/// Stored inside the world / level folder.
		WORLD
	}
}