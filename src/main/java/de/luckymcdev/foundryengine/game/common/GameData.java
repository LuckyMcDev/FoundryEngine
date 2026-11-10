package de.luckymcdev.foundryengine.game.common;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.Common;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistable data container identified by an Identifier, backed by NBT compounds.
 * <p>
 * Data is scoped per world: a {@code GameData} starts out detached and is bound to a
 * world's per-world tag store via {@link #bindWorld(String)}, which happens when the
 * owning {@link GameSession} is loaded/saved for that world. Two sessions with the same
 * ID in different worlds therefore get distinct tags.
 */
public class GameData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String INITIALIZED_KEY = "_initialized";
	private final Identifier identifier;
	private volatile CompoundTag data;
	private @Nullable String worldName;
	private @Nullable Runnable initHandler;

	public GameData(Identifier identifier) {
		this.identifier = identifier;
		this.data = new CompoundTag();
	}

	/**
	 * Binds this data to the given world's per-world tag store. Called by
	 * {@link GameSession} when the session is loaded/saved for that world.
	 */
	void bindWorld(String worldName) {
		this.worldName = worldName;
		this.data = Common.getGameManager().getOrCreateSessionData(worldName, identifier);
	}

	/**
	 * Returns the world this data is currently bound to, or {@code null} if detached.
	 */
	public @Nullable String worldName() {
		return worldName;
	}

	/**
	 * Returns the identifier for this game data.
	 */
	public Identifier identifier() {
		return identifier;
	}

	/**
	 * Returns the underlying NBT compound data.
	 * <p>
	 * The tag is the per-world tag for this data's ID. It survives bundle reloads and is
	 * shared by all sessions with the same ID <em>in the same world</em>.
	 * <p>
	 * Threading contract: a session can be ticked from both the client and the server
	 * thread in the same process (integrated server), so the shared tag may be accessed
	 * concurrently. Every read/write that goes through the accessors is guarded by
	 * synchronizing on the tag itself. If the returned tag is mutated directly, the
	 * caller must synchronize on that tag object as well to avoid races.
	 */
	public CompoundTag data() {
		return data;
	}

	/**
	 * Returns true if this data has been initialized at least once.
	 */
	public boolean isInitialized() {
		CompoundTag tag = data;
		synchronized (tag) {
			return tag.getBoolean(INITIALIZED_KEY).orElse(false);
		}
	}

	/**
	 * Sets the initialization flag for this data.
	 */
	public void setInitialized(boolean initialized) {
		CompoundTag tag = data;
		synchronized (tag) {
			tag.putBoolean(INITIALIZED_KEY, initialized);
		}
	}

	/**
	 * Registers a callback that fires once when the data is first loaded from disk.
	 */
	public GameData onInit(Runnable handler) {
		this.initHandler = handler;
		return this;
	}

	/**
	 * Gets a boolean value from the data.
	 */
	public boolean getBoolean(String key) {
		CompoundTag tag = data;
		synchronized (tag) {
			return tag.getBoolean(key).orElse(false);
		}
	}

	/**
	 * Puts a boolean value into the data.
	 */
	public void putBoolean(String key, boolean value) {
		CompoundTag tag = data;
		synchronized (tag) {
			tag.putBoolean(key, value);
		}
	}

	/**
	 * Gets a double value from the data.
	 */
	public double getDouble(String key) {
		CompoundTag tag = data;
		synchronized (tag) {
			return tag.getDouble(key).orElse(0.0);
		}
	}

	/**
	 * Puts a double value into the data.
	 */
	public void putDouble(String key, double value) {
		CompoundTag tag = data;
		synchronized (tag) {
			tag.putDouble(key, value);
		}
	}

	/**
	 * Gets an integer value from the data.
	 */
	public int getInt(String key) {
		CompoundTag tag = data;
		synchronized (tag) {
			return tag.getInt(key).orElse(0);
		}
	}

	/**
	 * Puts an integer value into the data.
	 */
	public void putInt(String key, int value) {
		CompoundTag tag = data;
		synchronized (tag) {
			tag.putInt(key, value);
		}
	}

	/**
	 * Gets a string value from the data, or null if absent.
	 */
	public @Nullable String getString(String key) {
		CompoundTag tag = data;
		synchronized (tag) {
			return tag.getString(key).orElse(null);
		}
	}

	/**
	 * Puts a string value into the data.
	 */
	public void putString(String key, String value) {
		CompoundTag tag = data;
		synchronized (tag) {
			tag.putString(key, value);
		}
	}

	protected void onSave(CompoundTag tag) {
		CompoundTag dataTag = data;
		synchronized (dataTag) {
			tag.put("data", dataTag);
		}
	}

	protected void onLoad(CompoundTag tag) {
		CompoundTag dataTag = data;
		synchronized (dataTag) {
			if (tag.contains("data")) {
				tag.getCompound("data").ifPresent(dataTag::merge);
			}
			if (!dataTag.getBoolean(INITIALIZED_KEY).orElse(false)) {
				if (initHandler != null) {
					initHandler.run();
				}
				dataTag.putBoolean(INITIALIZED_KEY, true);
			}
		}
	}

	/**
	 * Saves the game data to a compressed NBT file in the given directory.
	 */
	public final void saveTo(Path directory) {
		CompoundTag tag = new CompoundTag();
		onSave(tag);
		Path path = resolvePath(directory);
		CompoundTag dataTag = data;
		synchronized (dataTag) {
			try {
				Files.createDirectories(path.getParent());
				NbtIo.writeCompressed(tag, path);
			} catch (IOException e) {
				LOGGER.error("Failed to save game data [{}]", identifier, e);
			}
		}
	}

	/**
	 * Loads the game data from a compressed NBT file in the given directory.
	 */
	public final void loadFrom(Path directory) {
		Path path = resolvePath(directory);
		CompoundTag dataTag = data;
		synchronized (dataTag) {
			try {
				if (Files.exists(path)) {
					CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.defaultQuota());
					onLoad(tag);
				} else {
					if (initHandler != null) {
						initHandler.run();
					}
					dataTag.putBoolean(INITIALIZED_KEY, true);
				}
			} catch (IOException e) {
				LOGGER.error("Failed to load game data [{}]", identifier, e);
			}
		}
	}

	private Path resolvePath(Path directory) {
		return directory.resolve(identifier.getNamespace()).resolve(identifier.getPath() + ".nbt");
	}
}