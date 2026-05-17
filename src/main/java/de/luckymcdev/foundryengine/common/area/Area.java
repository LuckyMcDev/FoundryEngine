package de.luckymcdev.foundryengine.common.area;

import net.minecraft.core.GlobalPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record Area(String id, AABB bounds, ResourceKey<Level> dimension) {

    public static Area of(String id, Vec3 min, Vec3 max, ResourceKey<Level> dimension) {
        return new Area(id, new AABB(min, max), dimension);
    }

    public boolean contains(GlobalPos position) {
        return position.dimension() == dimension &&
                bounds.contains(position.pos().getX(), position.pos().getY(), position.pos().getZ());
    }

    public void drawDebugOutline(int color) {
        Gizmos.cuboid(bounds, GizmoStyle.stroke(color));
    }
}