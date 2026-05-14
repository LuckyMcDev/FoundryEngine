package de.luckymcdev.foundryengine.client.waypoint;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.Vec3i;

import java.util.HashSet;
import java.util.Set;

public class WaypointManager {
    public static Set<CWaypoint> waypoints = new HashSet<>();

    public static Set<CWaypoint> getWaypoints() {
        return waypoints;
    }

    public static void addWaypoint(Vec3i pos, Color color) {
        CWaypoint toAdd = new CWaypoint(pos, color);
        waypoints.remove(toAdd);
        waypoints.add(toAdd);
    }

    public static void removeWaypoint(int x, int y, int z) {
        CWaypoint toRemove = new CWaypoint(x, y, z, 0f, 0f, 0f, 0f);
        waypoints.remove(toRemove);
    }

    public static void clearWaypoints() {
        waypoints.clear();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean waypointExists(float x, float y, float z) {
        CWaypoint toCheck = new CWaypoint((int) x, (int) y, (int) z, 0f, 0f, 0f, 0f);
        return waypoints.contains(toCheck);
    }
}