package de.luckymcdev.foundryengine.common.waypoint;

import net.minecraft.nbt.CompoundTag;

/**
 * Legacy waypoint record kept for source compatibility.
 * Use {@link Waypoint} instead.
 */
@Deprecated(forRemoval = true)
public record WaypointData(String name, String icon, int x, int y, int z, int color) {

    public static WaypointData fromNbt(CompoundTag tag) {
        var w = Waypoint.fromNbt(tag);
        return new WaypointData(w.name(), w.icon(), w.x(), w.y(), w.z(), w.color());
    }

    public CompoundTag toNbt() {
        return new Waypoint(name, icon, x, y, z, color).toNbt();
    }
}
