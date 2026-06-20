package de.luckymcdev.foundryengine.common.area;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.ArrayList;
import java.util.List;

public class AreaSavedData extends SavedData {

    public static final SavedDataType<AreaSavedData> TYPE = new SavedDataType<>(
            Common.id("area_manager"),
            AreaSavedData::new,
            CompoundTag.CODEC.xmap(AreaSavedData::new, AreaSavedData::toNbt),
            DataFixTypes.LEVEL
    );

    private List<Area> areas = new ArrayList<>();

    public AreaSavedData() {
    }

    public AreaSavedData(CompoundTag tag) {
        this.areas = makeList(tag);
    }

    public static AreaSavedData get(ServerLevel level) {
        SavedDataStorage storage = level.getDataStorage();
        if (storage == null) {
            throw new IllegalStateException("Data storage is null");
        }
        return storage.computeIfAbsent(TYPE);
    }

    public static List<Area> makeList(CompoundTag tag) {
        List<Area> result = new ArrayList<>();
        ListTag list = tag.getListOrEmpty("Areas");
        for (int i = 0; i < list.size(); i++) {
            result.add(Area.readFromNbt(list.getCompoundOrEmpty(i)));
        }
        return result;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Area area : areas) {
            list.add(area.writeToNbt());
        }
        tag.put("Areas", list);
        return tag;
    }

    public List<Area> getAreas() {
        return new ArrayList<>(areas);
    }

    public void addArea(Area area) {
        for (int i = 0; i < areas.size(); i++) {
            if (areas.get(i).id().equals(area.id())) {
                areas.set(i, area);
                setDirty();
                return;
            }
        }
        areas.add(area);
        setDirty();
    }

    public boolean removeArea(Identifier id) {
        boolean removed = areas.removeIf(a -> a.id().equals(id));
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public boolean updateArea(Identifier id, Area updatedArea) {
        for (int i = 0; i < areas.size(); i++) {
            if (areas.get(i).id().equals(id)) {
                areas.set(i, updatedArea);
                setDirty();
                return true;
            }
        }
        return false;
    }
}
