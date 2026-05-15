package de.luckymcdev.foundryengine.client.waypoint;

import net.minecraft.core.Vec3i;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WaypointManager {
    private static final Map<Vec3i, Waypoint> waypoints = new HashMap<>();

    public static void addWaypoint(Waypoint wp) {
        waypoints.put(wp.position(), wp);
    }

    public static void removeWaypoint(Vec3i coords) {
        waypoints.remove(coords);
    }

    public static Collection<Waypoint> getWaypoints() {
        return waypoints.values();
    }

    public static void clearWaypoints() {
        waypoints.clear();
    }
}