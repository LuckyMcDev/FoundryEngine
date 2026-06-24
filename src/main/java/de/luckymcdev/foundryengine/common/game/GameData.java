package de.luckymcdev.foundryengine.common.game;

import com.mojang.logging.LogUtils;
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
    private final Identifier identifier;
    private CompoundTag data = new CompoundTag();

    public GameData(Identifier identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the identifier for this game data.
     */
    public Identifier identifier() {
        return identifier;
    }

    /**
     * Returns the underlying NBT compound data.
     */
    public CompoundTag data() {
        return data;
    }

    /**
     * Sets the underlying NBT compound data.
     */
    public void data(CompoundTag data) {
        this.data = data;
    }

    /**
     * Gets a boolean value from the data.
     */
    public boolean getBoolean(String key) {
        return data.getBoolean(key).orElse(false);
    }

    /**
     * Puts a boolean value into the data.
     */
    public void putBoolean(String key, boolean value) {
        data.putBoolean(key, value);
    }

    /**
     * Gets a double value from the data.
     */
    public double getDouble(String key) {
        return data.getDouble(key).orElse(0.0);
    }

    /**
     * Puts a double value into the data.
     */
    public void putDouble(String key, double value) {
        data.putDouble(key, value);
    }

    /**
     * Gets an integer value from the data.
     */
    public int getInt(String key) {
        return data.getInt(key).orElse(0);
    }

    /**
     * Puts an integer value into the data.
     */
    public void putInt(String key, int value) {
        data.putInt(key, value);
    }

    /**
     * Gets a string value from the data, or null if absent.
     */
    public @Nullable String getString(String key) {
        return data.getString(key).orElse(null);
    }

    /**
     * Puts a string value into the data.
     */
    public void putString(String key, String value) {
        data.putString(key, value);
    }

    protected void onSave(CompoundTag tag) {
        tag.put("data", data);
    }

    protected void onLoad(CompoundTag tag) {
        if (tag.contains("data")) {
            tag.getCompound("data").ifPresent(loaded -> data = loaded);
        }
    }

    /**
     * Saves the game data to a compressed NBT file in the given directory.
     */
    public final void saveTo(Path directory) {
        CompoundTag tag = new CompoundTag();
        onSave(tag);
        Path path = resolvePath(directory);
        try {
            Files.createDirectories(path.getParent());
            NbtIo.writeCompressed(tag, path);
        } catch (IOException e) {
            LOGGER.error("Failed to save game data [{}]", identifier, e);
        }
    }

    /**
     * Loads the game data from a compressed NBT file in the given directory.
     */
    public final void loadFrom(Path directory) {
        Path path = resolvePath(directory);
        try {
            if (Files.exists(path)) {
                CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.defaultQuota());
                onLoad(tag);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load game data [{}]", identifier, e);
        }
    }

    private Path resolvePath(Path directory) {
        return directory.resolve(identifier.getNamespace()).resolve(identifier.getPath() + ".nbt");
    }
}
