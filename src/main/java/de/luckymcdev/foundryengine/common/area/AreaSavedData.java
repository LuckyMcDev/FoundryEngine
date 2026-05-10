package de.luckymcdev.foundryengine.common.area;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
        this.areas = fromNbt(tag);
    }

    public static AreaSavedData get(ServerLevel level) {
        SavedDataStorage storage = level.getDataStorage();
        if (storage == null) {
            throw new IllegalStateException("Data storage is null");
        }
        return storage.computeIfAbsent(TYPE);
    }

    private static List<Area> fromNbt(CompoundTag tag) {
        List<Area> result = new ArrayList<>();
        ListTag list = tag.getListOrEmpty("Areas");
        for (int i = 0; i < list.size(); i++) {
            result.add(areaFromNbt(list.getCompoundOrEmpty(i)));
        }
        return result;
    }

    private static CompoundTag areaToNbt(Area area) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", area.id());
        tag.putString("dimension", area.dimension().identifier().toString());
        AABB b = area.bounds();
        tag.putDouble("minX", b.minX);
        tag.putDouble("minY", b.minY);
        tag.putDouble("minZ", b.minZ);
        tag.putDouble("maxX", b.maxX);
        tag.putDouble("maxY", b.maxY);
        tag.putDouble("maxZ", b.maxZ);
        return tag;
    }

    private static Area areaFromNbt(CompoundTag tag) {
        String id = tag.getString("id").orElse("");
        ResourceKey<Level> dimension = ResourceKey.create(
                Registries.DIMENSION,
                Identifier.parse(tag.getString("dimension").orElse("minecraft:overworld"))
        );
        Vec3 min = new Vec3(
                tag.getDouble("minX").orElse(0D),
                tag.getDouble("minY").orElse(0D),
                tag.getDouble("minZ").orElse(0D)
        );
        Vec3 max = new Vec3(
                tag.getDouble("maxX").orElse(0D),
                tag.getDouble("maxY").orElse(0D),
                tag.getDouble("maxZ").orElse(0D)
        );
        return Area.of(id, min, max, dimension);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Area area : areas) {
            list.add(areaToNbt(area));
        }
        tag.put("Areas", list);
        return tag;
    }

    public List<Area> getAreas() {
        return new ArrayList<>(areas);
    }

    public void addArea(Area area) {
        areas.add(area);
        setDirty();
    }

    public boolean removeArea(String id) {
        boolean removed = areas.removeIf(a -> a.id().equals(id));
        if (removed) {
            setDirty();
        }
        return removed;
    }
}