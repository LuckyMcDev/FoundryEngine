package de.luckymcdev.foundryengine.common.area;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record Area(String id, AABB bounds, ResourceKey<Level> dimension, int color) {
    public static final int DEFAULT_COLOR = 0xFFFF4444;

    public static Area of(String id, Vec3 min, Vec3 max, ResourceKey<Level> dimension, int color) {
        return new Area(id, new AABB(min, max), dimension, color);
    }

    public boolean contains(GlobalPos position) {
        return position.dimension() == dimension &&
                bounds.contains(position.pos().getX(), position.pos().getY(), position.pos().getZ());
    }

    public static CompoundTag writeToNbt(Area area) {
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
        tag.putInt("color", area.color());
        return tag;
    }

    public static Area readFromNbt(CompoundTag tag) {
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
        int color = tag.getInt("color").orElse(DEFAULT_COLOR);
        return new Area(id, new AABB(min, max), dimension, color);
    }

    public void drawDebugOutline() {
        Gizmos.cuboid(bounds, GizmoStyle.stroke(color));
    }
}
