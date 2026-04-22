package de.luckymcdev.foundryengine.common.cutscene.storage;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.network.CutscenePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CutsceneSavedData extends SavedData {
    public static final SavedDataType<CutsceneSavedData> TYPE = new SavedDataType<>(
            Common.id("cutscene_manager"),
            CutsceneSavedData::new,
            CompoundTag.CODEC.xmap(CutsceneSavedData::new, CutsceneSavedData::getData),
            DataFixTypes.LEVEL
    );

    private CompoundTag data = new CompoundTag();

    public CutsceneSavedData() {
    }

    public CutsceneSavedData(CompoundTag tag) {
        this.data = tag;
    }

    public static CutsceneSavedData get(ServerLevel level) {
        SavedDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public static List<Cutscene> makeList(CompoundTag tag) {
        ListTag list = tag.getListOrEmpty("CutsceneList");
        List<Cutscene> cutscenes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            cutscenes.add(Cutscene.fromNbt(list.getCompoundOrEmpty(i)));
        }
        return cutscenes;
    }

    public CompoundTag getData() {
        return data;
    }

    public void setData(CompoundTag tag) {
        this.data = tag;
        this.setDirty();
    }

    public Collection<Identifier> getSuggestions() {
        ArrayList<Identifier> names = new ArrayList<>();
        for (Cutscene cutscene : getCutscenes()) {
            names.add(Common.id(cutscene.getName()));
        }
        return names;
    }

    public List<Cutscene> getCutscenes() {
        return makeList(this.data);
    }

    public void setCutscenes(List<Cutscene> cutscenes) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Cutscene cutscene : cutscenes) {
            list.add(cutscene.toNbt());
        }
        tag.put("CutsceneList", list);
        this.data = tag;
        this.setDirty();
    }

    public void syncToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new CutscenePacket(this.data));
    }

    public void syncToClients(ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, new CutscenePacket(this.data));
    }
}
