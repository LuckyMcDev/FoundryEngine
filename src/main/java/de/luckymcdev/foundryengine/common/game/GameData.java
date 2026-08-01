package de.luckymcdev.foundryengine.common.game;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
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
 */
public class GameData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String INITIALIZED_KEY = "_initialized";
	private final Identifier identifier;
	private final CompoundTag data;
	private @Nullable Runnable initHandler;

	public GameData(Identifier identifier) {
		this.identifier = identifier;
		this.data = Common.getGameManager().getOrCreateSessionData(identifier);
	}

	/**
	 * Returns the identifier for this game data.
	 */
	public Identifier identifier() {
		return identifier;
	}

	/**
	 * Returns the underlying NBT compound data.
	 * The returned tag is shared via GameManager's persistent store and survives
	 * bundle reloads — modifications are visible to all sessions with the same ID.
	 * <p>
	 * Threading contract: a session can be ticked from both the client and the server
	 * thread in the same process (integrated server), so the shared tag may be accessed
	 * concurrently. Every read/write that goes through the  accessors is
	 * guarded by synchronizing on the tag itself. If the returned tag is mutated directly,
	 * the caller must synchronize on that tag object as well to avoid races.
	 */
	public CompoundTag data() {
		return data;
	}

	/**
	 * Returns true if this data has been initialized at least once.
	 */
	public boolean isInitialized() {
		synchronized (data) {
			return data.getBoolean(INITIALIZED_KEY).orElse(false);
		}
	}

	/**
	 * Sets the initialization flag for this data.
	 */
	public void setInitialized(boolean initialized) {
		synchronized (data) {
			data.putBoolean(INITIALIZED_KEY, initialized);
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
		synchronized (data) {
			return data.getBoolean(key).orElse(false);
		}
	}

	/**
	 * Puts a boolean value into the data.
	 */
	public void putBoolean(String key, boolean value) {
		synchronized (data) {
			data.putBoolean(key, value);
		}
	}

	/**
	 * Gets a double value from the data.
	 */
	public double getDouble(String key) {
		synchronized (data) {
			return data.getDouble(key).orElse(0.0);
		}
	}

	/**
	 * Puts a double value into the data.
	 */
	public void putDouble(String key, double value) {
		synchronized (data) {
			data.putDouble(key, value);
		}
	}

	/**
	 * Gets an integer value from the data.
	 */
	public int getInt(String key) {
		synchronized (data) {
			return data.getInt(key).orElse(0);
		}
	}

	/**
	 * Puts an integer value into the data.
	 */
	public void putInt(String key, int value) {
		synchronized (data) {
			data.putInt(key, value);
		}
	}

	/**
	 * Gets a string value from the data, or null if absent.
	 */
	public @Nullable String getString(String key) {
		synchronized (data) {
			return data.getString(key).orElse(null);
		}
	}

	/**
	 * Puts a string value into the data.
	 */
	public void putString(String key, String value) {
		synchronized (data) {
			data.putString(key, value);
		}
	}

	protected void onSave(CompoundTag tag) {
		synchronized (data) {
			tag.put("data", data);
		}
	}

	protected void onLoad(CompoundTag tag) {
		synchronized (data) {
			if (tag.contains("data")) {
				tag.getCompound("data").ifPresent(data::merge);
			}
			if (!isInitialized()) {
				if (initHandler != null) {
					initHandler.run();
				}
				setInitialized(true);
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
		synchronized (data) {
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
		synchronized (data) {
			try {
				if (Files.exists(path)) {
					CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.defaultQuota());
					onLoad(tag);
				} else {
					if (initHandler != null) {
						initHandler.run();
					}
					setInitialized(true);
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
