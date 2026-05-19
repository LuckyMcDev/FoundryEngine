package de.luckymcdev.foundryengine.common.savedata;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.sync.SavedDataSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SavedDataManager {
    private final Map<Identifier, Function<ServerLevel, CompoundTag>> collectors = new LinkedHashMap<>();
    private final Map<Identifier, Consumer<CompoundTag>> clientHandlers = new HashMap<>();

    public void register(Identifier id, Function<ServerLevel, CompoundTag> collector, Consumer<CompoundTag> clientHandler) {
        collectors.put(id, collector);
        if (clientHandler != null) {
            clientHandlers.put(id, clientHandler);
        }
    }

    public void registerClientHandler(Identifier id, Consumer<CompoundTag> handler) {
        clientHandlers.put(id, handler);
    }

    public CompoundTag collectAll(ServerLevel level) {
        CompoundTag result = new CompoundTag();
        for (var entry : collectors.entrySet()) {
            CompoundTag data = entry.getValue().apply(level);
            result.put(entry.getKey().toString(), data);
        }
        return result;
    }

    public void dispatchSync(CompoundTag data) {
        for (String key : data.keySet()) {
            Identifier id = Identifier.parse(key);
            Consumer<CompoundTag> handler = clientHandlers.get(id);
            if (handler != null) {
                handler.accept(data.getCompound(key).orElse(new CompoundTag()));
            }
        }
    }

    public void syncToPlayer(ServerPlayer player) {
        CompoundTag data = collectAll(player.level());
        Common.getNetworkManager().sendToPlayer(new SavedDataSyncPacket(data), player);
    }

    public void syncToDimension(ServerLevel level) {
        CompoundTag data = collectAll(level);
        PacketDistributor.sendToPlayersInDimension(level, new SavedDataSyncPacket(data));
    }
}
