package de.luckymcdev.foundryengine.common.area.preset;

import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AreaPreset {
    private final String id;
    private final Color defaultColor;
    private final List<Identifier> moduleIds;
    private final Map<Identifier, CompoundTag> defaultModuleData;

    private AreaPreset(String id, Color defaultColor, List<Identifier> moduleIds, Map<Identifier, CompoundTag> defaultModuleData) {
        this.id = id;
        this.defaultColor = defaultColor;
        this.moduleIds = List.copyOf(moduleIds);
        this.defaultModuleData = Map.copyOf(defaultModuleData);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static AreaPreset readFromNbt(CompoundTag tag) {
        String id = tag.getString("id").orElse("");
        Color color = new Color(tag.getInt("defaultColor").orElse(Area.DEFAULT_COLOR.argb()));
        Builder builder = builder(id).color(color);
        var modList = tag.getListOrEmpty("moduleIds");
        for (int i = 0; i < modList.size(); i++) {
            modList.getString(i).ifPresent(s -> builder.module(Identifier.parse(s)));
        }
        var dataTag = tag.getCompound("defaultModuleData").orElse(new CompoundTag());
        for (String key : dataTag.keySet()) {
            dataTag.getCompound(key).ifPresent(ct -> builder.moduleData(Identifier.parse(key), ct));
        }
        return builder.build();
    }

    public String id() {
        return id;
    }

    public Color defaultColor() {
        return defaultColor;
    }

    public List<Identifier> moduleIds() {
        return moduleIds;
    }

    public Map<Identifier, CompoundTag> defaultModuleData() {
        return defaultModuleData;
    }

    public Area create(Identifier areaId, Vec3 min, Vec3 max, ResourceKey<Level> dimension) {
        Area area = new Area(areaId, new AABB(min, max), dimension, defaultColor);
        for (Identifier mid : moduleIds) {
            area.addModule(mid);
        }
        for (var entry : defaultModuleData.entrySet()) {
            area.setModuleData(entry.getKey(), entry.getValue().copy());
        }
        return area;
    }

    public Area create(Identifier areaId, Vec3 min, Vec3 max, ResourceKey<Level> dimension, Color color) {
        var area = create(areaId, min, max, dimension);
        area.setColor(color);
        return area;
    }

    public CompoundTag writeToNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putInt("defaultColor", defaultColor.argb());
        ListTag modList = new ListTag();
        for (Identifier mid : moduleIds) {
            modList.add(StringTag.valueOf(mid.toString()));
        }
        tag.put("moduleIds", modList);
        CompoundTag dataTag = new CompoundTag();
        for (var entry : defaultModuleData.entrySet()) {
            dataTag.put(entry.getKey().toString(), entry.getValue());
        }
        tag.put("defaultModuleData", dataTag);
        return tag;
    }

    public static class Builder {
        private final String id;
        private final List<Identifier> moduleIds = new ArrayList<>();
        private final Map<Identifier, CompoundTag> defaultModuleData = new HashMap<>();
        private Color defaultColor = Area.DEFAULT_COLOR;

        private Builder(String id) {
            this.id = id;
        }

        public Builder color(Color color) {
            this.defaultColor = color;
            return this;
        }

        public Builder module(Identifier moduleId) {
            this.moduleIds.add(moduleId);
            return this;
        }

        public Builder moduleData(Identifier moduleId, CompoundTag data) {
            this.defaultModuleData.put(moduleId, data);
            if (!moduleIds.contains(moduleId)) {
                moduleIds.add(moduleId);
            }
            return this;
        }

        public Builder moduleData(Identifier moduleId, Consumer<CompoundTag> consumer) {
            CompoundTag tag = new CompoundTag();
            consumer.accept(tag);
            return moduleData(moduleId, tag);
        }

        public AreaPreset build() {
            return new AreaPreset(id, defaultColor, moduleIds, defaultModuleData);
        }
    }
}
