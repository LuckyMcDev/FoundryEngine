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

public class GameData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Identifier identifier;
    private CompoundTag data = new CompoundTag();

    public GameData(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier identifier() {
        return identifier;
    }

    public CompoundTag data() {
        return data;
    }

    public void data(CompoundTag data) {
        this.data = data;
    }

    public boolean getBoolean(String key) {
        return data.getBoolean(key).orElse(false);
    }

    public void putBoolean(String key, boolean value) {
        data.putBoolean(key, value);
    }

    public double getDouble(String key) {
        return data.getDouble(key).orElse(0.0);
    }

    public void putDouble(String key, double value) {
        data.putDouble(key, value);
    }

    public int getInt(String key) {
        return data.getInt(key).orElse(0);
    }

    public void putInt(String key, int value) {
        data.putInt(key, value);
    }

    public @Nullable String getString(String key) {
        return data.getString(key).orElse(null);
    }

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
