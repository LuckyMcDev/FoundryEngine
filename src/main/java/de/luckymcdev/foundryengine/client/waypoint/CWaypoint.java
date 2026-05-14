package de.luckymcdev.foundryengine.client.waypoint;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.Vec3i;

public class CWaypoint {
    private final Vec3i position;
    private final Color color;

    public CWaypoint(int x, int y, int z, float r, float g, float b, float a) {
        this.position = new Vec3i(x, y, z);
        this.color = new Color(r, g, b, a);
    }

    public CWaypoint(Vec3i pos, Color color) {
        this.position = pos;
        this.color = color;
    }

    public boolean equals(Object o) {
        if (o instanceof CWaypoint wp) {
            return position.equals(wp.getPosition());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return position.hashCode();
    }


    public Vec3i getPosition() {
        return this.position;
    }

    public int getX() {
        return this.position.getX();
    }

    public int getY() {
        return this.position.getY();
    }

    public int getZ() {
        return this.position.getZ();
    }

    public Color getColour() {
        return this.color;
    }
}
