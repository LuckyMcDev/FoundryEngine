package de.luckymcdev.foundryengine.common.savedata;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.sync.SavedDataSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class SavedDataManager {
    private CompoundTag data = new CompoundTag();

    public void load() {
        if (Files.exists(Common.GAME_DATA)) {
            try (DataInputStream dis = new DataInputStream(Files.newInputStream(Common.GAME_DATA))) {
                data = NbtIo.read(dis);
            } catch (IOException e) {
                Common.LOGGER.error("Failed to load game data", e);
                data = new CompoundTag();
            }
        }
    }

    public void save() {
        try {
            Files.createDirectories(Common.GAME_DATA.getParent());
            try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Common.GAME_DATA))) {
                NbtIo.write(data, dos);
            }
        } catch (IOException e) {
            Common.LOGGER.error("Failed to save game data", e);
        }
    }

    public CompoundTag getData() {
        return data;
    }

    public void setData(CompoundTag tag) {
        this.data = tag;
        save();
    }

    public CompoundTag getSection(String key) {
        return data.getCompound(key).orElseGet(CompoundTag::new);
    }

    public void setSection(String key, CompoundTag section) {
        data.put(key, section);
        save();
    }

    public void syncToPlayer(ServerPlayer player) {
        Common.getNetworkManager().sendToPlayer(new SavedDataSyncPacket(data.copy()), player);
    }

    public void syncToAll() {
        Common.getNetworkManager().sendToAllPlayers(new SavedDataSyncPacket(data.copy()));
    }

}
