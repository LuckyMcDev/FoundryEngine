package de.luckymcdev.foundryengine.common.waypoint;

import net.minecraft.nbt.CompoundTag;

public record WaypointData(String name, String icon, int x, int y, int z, int color) {

    public static WaypointData fromNbt(CompoundTag tag) {
        return new WaypointData(
                tag.getString("name").orElse(""),
                tag.getString("icon").orElse("I"),
                tag.getInt("x").orElse(0),
                tag.getInt("y").orElse(0),
                tag.getInt("z").orElse(0),
                tag.getInt("color").orElse(0xFFFFFFFF)
        );
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("icon", icon);
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        tag.putInt("color", color);
        return tag;
    }
}
