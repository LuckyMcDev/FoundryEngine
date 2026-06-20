package de.luckymcdev.foundryengine.common.area;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.BlockPos;
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

public class AABBArea extends Area {
    private AABB bounds;

    public AABBArea(Identifier id, AABB bounds, ResourceKey<Level> dimension, Color color) {
        super(id, dimension, color);
        this.bounds = bounds;
    }

    public static AABBArea of(Identifier id, Vec3 min, Vec3 max, ResourceKey<Level> dimension, Color color) {
        return new AABBArea(id, new AABB(min, max), dimension, color);
    }

    public static AABBArea readFromNbt(CompoundTag tag) {
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
        return new AABBArea(id, new AABB(min, max), dimension, color);
    }

    @Override
    public AABB bounds() { return bounds; }

    public void setBounds(AABB bounds) { this.bounds = bounds; }

    @Override
    public boolean contains(GlobalPos position) {
        return position.dimension() == dimension() &&
                bounds.contains(position.pos().getX(), position.pos().getY(), position.pos().getZ());
    }

    @Override
    public boolean contains(BlockPos pos) {
        return bounds.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean contains(Vec3 pos) {
        return bounds.contains(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return bounds.contains(x, y, z);
    }

    @Override
    public void drawDebugOutline() {
        Gizmos.cuboid(bounds, GizmoStyle.stroke(color().argb(), 4.0f));
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag tag = writeSharedNbt();
        tag.putString("type", "aabb");
        tag.putDouble("minX", bounds.minX);
        tag.putDouble("minY", bounds.minY);
        tag.putDouble("minZ", bounds.minZ);
        tag.putDouble("maxX", bounds.maxX);
        tag.putDouble("maxY", bounds.maxY);
        tag.putDouble("maxZ", bounds.maxZ);
        return tag;
    }

    @Override
    public String toString() {
        return "AABBArea[" +
                "id=" + id() +
                ", bounds=" + bounds +
                ", dimension=" + dimension().identifier() +
                ", color=" + color() +
                ", modules=" + moduleIds() +
                ']';
    }
}
