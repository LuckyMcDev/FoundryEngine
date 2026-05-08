package de.luckymcdev.foundryengine.common.scene.storage;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.scene.SceneGraph;
import de.luckymcdev.foundryengine.common.scene.network.ScenePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Per-dimension persisted scene graph.
 */
public class SceneSavedData extends SavedData {
    public static final SavedDataType<SceneSavedData> TYPE = new SavedDataType<>(
            Common.id("scene_graph"),
            SceneSavedData::new,
            CompoundTag.CODEC.xmap(SceneSavedData::new, SceneSavedData::getData),
            DataFixTypes.LEVEL
    );

    private CompoundTag data = new CompoundTag();

    public SceneSavedData() {
    }

    public SceneSavedData(CompoundTag tag) {
        this.data = (tag != null) ? tag : new CompoundTag();
    }

    public static SceneSavedData get(ServerLevel level) {
        SavedDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public CompoundTag getData() {
        return data;
    }

    public void setData(CompoundTag tag) {
        this.data = (tag != null) ? tag : new CompoundTag();
        this.setDirty();
    }

    public SceneGraph getGraph() {
        return SceneGraph.fromNbt(data);
    }

    public void setGraph(SceneGraph graph) {
        setData(graph != null ? graph.toNbt() : new SceneGraph().toNbt());
    }

    public void syncToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ScenePacket(this.data));
    }

    public void syncToClients(ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, new ScenePacket(this.data));
    }
}

