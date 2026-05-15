package de.luckymcdev.foundryengine.client.waypoint;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public record Waypoint(Component icon, String name, Vec3i position, Color color) {

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public int getZ() {
        return position.getZ();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waypoint waypoint = (Waypoint) o;
        return Objects.equals(position, waypoint.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }
}