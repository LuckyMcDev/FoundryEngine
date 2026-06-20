package de.luckymcdev.foundryengine.common.area;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class Area {
    public static final Color DEFAULT_COLOR = new Color(255, 68, 68);

    private final Identifier id;
    private final ResourceKey<Level> dimension;
    private final List<Identifier> moduleIds = new ArrayList<>();
    private final Map<Identifier, CompoundTag> moduleData = new HashMap<>();
    private final Map<String, Identifier> linkedAreas = new HashMap<>();
    private AABB bounds;
    private Color color;

    public Area(Identifier id, AABB bounds, ResourceKey<Level> dimension, Color color) {
        this.id = id;
        this.bounds = bounds;
        this.dimension = dimension;
        this.color = color;
    }

    public static Area of(Identifier id, Vec3 min, Vec3 max, ResourceKey<Level> dimension, Color color) {
        return new Area(id, new AABB(min, max), dimension, color);
    }

    public static Area readFromNbt(CompoundTag tag) {
        Identifier id = Identifier.parse(tag.getString("id").orElse("foundryengine:unknown"));
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
        Color color = new Color(tag.getInt("color").orElse(DEFAULT_COLOR.argb()));

        Area area = new Area(id, new AABB(min, max), dimension, color);

        ListTag modList = tag.getListOrEmpty("modules");
        for (int i = 0; i < modList.size(); i++) {
            modList.getString(i).ifPresent(s -> area.moduleIds.add(Identifier.parse(s)));
        }

        CompoundTag dataTag = tag.getCompound("moduleData").orElse(new CompoundTag());
        for (String key : dataTag.keySet()) {
            dataTag.getCompound(key).ifPresent(ct -> area.moduleData.put(Identifier.parse(key), ct));
        }

        CompoundTag linkTag = tag.getCompound("linkedAreas").orElse(new CompoundTag());
        for (String key : linkTag.keySet()) {
            linkTag.getString(key).ifPresent(value -> area.linkedAreas.put(key, Identifier.parse(value)));
        }

        return area;
    }

    public Identifier id() {
        return id;
    }

    public AABB bounds() {
        return bounds;
    }

    public void setBounds(AABB bounds) {
        this.bounds = bounds;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public Color color() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<Identifier> moduleIds() {
        return Collections.unmodifiableList(moduleIds);
    }

    public void addModule(Identifier moduleId) {
        if (!moduleIds.contains(moduleId)) {
            moduleIds.add(moduleId);
        }
    }

    public void removeModule(Identifier moduleId) {
        moduleIds.remove(moduleId);
        moduleData.remove(moduleId);
    }

    public boolean hasModule(Identifier moduleId) {
        return moduleIds.contains(moduleId);
    }

    public void clearModules() {
        moduleIds.clear();
        moduleData.clear();
    }

    public Map<Identifier, CompoundTag> moduleData() {
        return Collections.unmodifiableMap(moduleData);
    }

    public CompoundTag getModuleData(Identifier moduleId) {
        return moduleData.computeIfAbsent(moduleId, k -> new CompoundTag());
    }

    public void setModuleData(Identifier moduleId, CompoundTag data) {
        moduleData.put(moduleId, data);
        if (!moduleIds.contains(moduleId)) {
            moduleIds.add(moduleId);
        }
    }

    public boolean hasModuleData(Identifier moduleId) {
        return moduleData.containsKey(moduleId);
    }

    public Map<String, Identifier> linkedAreas() {
        return Collections.unmodifiableMap(linkedAreas);
    }

    public void linkArea(String name, Identifier areaId) {
        linkedAreas.put(name, areaId);
    }

    public void unlinkArea(String name) {
        linkedAreas.remove(name);
    }

    @Nullable
    public Identifier getLinkedArea(String name) {
        return linkedAreas.get(name);
    }

    public boolean contains(GlobalPos position) {
        return position.dimension() == dimension &&
                bounds.contains(position.pos().getX(), position.pos().getY(), position.pos().getZ());
    }

    public boolean contains(BlockPos pos) {
        return bounds.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean contains(Vec3 pos) {
        return bounds.contains(pos.x, pos.y, pos.z);
    }

    public boolean contains(double x, double y, double z) {
        return bounds.contains(x, y, z);
    }

    public void drawDebugOutline() {
        Gizmos.cuboid(bounds, GizmoStyle.stroke(color.argb()));
    }

    public CompoundTag writeToNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putString("dimension", dimension.identifier().toString());
        tag.putDouble("minX", bounds.minX);
        tag.putDouble("minY", bounds.minY);
        tag.putDouble("minZ", bounds.minZ);
        tag.putDouble("maxX", bounds.maxX);
        tag.putDouble("maxY", bounds.maxY);
        tag.putDouble("maxZ", bounds.maxZ);
        tag.putInt("color", color.argb());

        if (!moduleIds.isEmpty()) {
            ListTag modList = new ListTag();
            for (Identifier mid : moduleIds) {
                modList.add(StringTag.valueOf(mid.toString()));
            }
            tag.put("modules", modList);
        }

        if (!moduleData.isEmpty()) {
            CompoundTag dataTag = new CompoundTag();
            for (var entry : moduleData.entrySet()) {
                dataTag.put(entry.getKey().toString(), entry.getValue());
            }
            tag.put("moduleData", dataTag);
        }

        if (!linkedAreas.isEmpty()) {
            CompoundTag linkTag = new CompoundTag();
            for (var entry : linkedAreas.entrySet()) {
                linkTag.putString(entry.getKey(), entry.getValue().toString());
            }
            tag.put("linkedAreas", linkTag);
        }

        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Area area)) return false;
        return id.equals(area.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Area[" +
                "id=" + id +
                ", bounds=" + bounds +
                ", dimension=" + dimension.identifier() +
                ", color=" + color +
                ", modules=" + moduleIds +
                ']';
    }
}
