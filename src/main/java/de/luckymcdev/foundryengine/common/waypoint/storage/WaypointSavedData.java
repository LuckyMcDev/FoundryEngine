package de.luckymcdev.foundryengine.common.waypoint.storage;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.ArrayList;
import java.util.List;

public class WaypointSavedData extends SavedData {

    public static final SavedDataType<WaypointSavedData> TYPE = new SavedDataType<>(
            Common.id("waypoint_manager"),
            WaypointSavedData::new,
            CompoundTag.CODEC.xmap(WaypointSavedData::new, WaypointSavedData::toNbt),
            DataFixTypes.LEVEL
    );

    private CompoundTag data = new CompoundTag();

    public WaypointSavedData() {
    }

    public WaypointSavedData(CompoundTag tag) {
        this.data = (tag != null) ? tag : new CompoundTag();
    }

    public static WaypointSavedData get(ServerLevel level) {
        SavedDataStorage storage = level.getDataStorage();
        if (storage == null) {
            throw new IllegalStateException("Data storage is null");
        }
        return storage.computeIfAbsent(TYPE);
    }

    public CompoundTag getData() {
        return data;
    }

    public void setData(CompoundTag tag) {
        this.data = (tag != null) ? tag : new CompoundTag();
        setDirty();
    }

    public List<CompoundTag> getWaypoints() {
        ListTag list = data.getListOrEmpty("Waypoints");
        List<CompoundTag> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(list.getCompoundOrEmpty(i));
        }
        return result;
    }

    public void addWaypoint(int x, int y, int z, String name, String icon, Color color) {
        ListTag list = data.getListOrEmpty("Waypoints");
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        tag.putString("name", name);
        tag.putString("icon", icon);
        tag.putInt("color", color.argb());
        list.add(tag);
        data.put("Waypoints", list);
        setDirty();
    }

    public boolean removeWaypoint(int x, int y, int z) {
        ListTag list = data.getListOrEmpty("Waypoints");
        boolean removed = false;
        for (int i = list.size() - 1; i >= 0; i--) {
            CompoundTag tag = list.getCompoundOrEmpty(i);
            if (tag.getInt("x").orElse(0) == x && tag.getInt("y").orElse(0) == y && tag.getInt("z").orElse(0) == z) {
                list.remove(i);
                removed = true;
            }
        }
        if (removed) {
            data.put("Waypoints", list);
            setDirty();
        }
        return removed;
    }

    public void clearWaypoints() {
        data.put("Waypoints", new ListTag());
        setDirty();
    }

    public CompoundTag toNbt() {
        return data;
    }
}
