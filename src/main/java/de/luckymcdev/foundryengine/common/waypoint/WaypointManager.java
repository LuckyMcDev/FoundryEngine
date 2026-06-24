package de.luckymcdev.foundryengine.common.waypoint;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.waypoint.storage.WaypointSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Manages waypoints per dimension with persistence and player sync.
 */
public class WaypointManager {
    private final Map<ResourceKey<Level>, List<Waypoint>> waypointsByDimension = new HashMap<>();

    /**
     * Checks if waypoints for the given dimension are loaded.
     */
    public boolean isLoaded(ResourceKey<Level> dimension) {
        return waypointsByDimension.containsKey(dimension);
    }

    /**
     * Loads waypoints from level saved data for the given dimension.
     */
    public void loadFromLevel(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        WaypointSavedData savedData = WaypointSavedData.get(level);
        List<Waypoint> loaded = new ArrayList<>();
        ListTag list = savedData.getData().getListOrEmpty("Waypoints");
        for (int i = 0; i < list.size(); i++) {
            loaded.add(Waypoint.fromNbt(list.getCompoundOrEmpty(i)));
        }
        waypointsByDimension.put(dimension, loaded);
    }

    /**
     * Returns the list of waypoints for the given dimension.
     */
    public List<Waypoint> getWaypoints(ResourceKey<Level> dimension) {
        return waypointsByDimension.getOrDefault(dimension, Collections.emptyList());
    }

    /**
     * Adds a waypoint and persists it to the level's saved data.
     */
    public void addWaypoint(ServerLevel level, Waypoint waypoint) {
        ResourceKey<Level> dimension = level.dimension();
        waypointsByDimension.computeIfAbsent(dimension, k -> new ArrayList<>()).add(waypoint);
        persist(level);
    }

    /**
     * Adds a waypoint locally without persisting to disk.
     */
    public void addLocal(ResourceKey<Level> dimension, Waypoint waypoint) {
        waypointsByDimension.computeIfAbsent(dimension, k -> new ArrayList<>()).add(waypoint);
    }

    /**
     * Removes a waypoint by coordinates and persists the change.
     */
    public boolean removeWaypoint(ServerLevel level, int x, int y, int z) {
        ResourceKey<Level> dimension = level.dimension();
        boolean removed = false;
        List<Waypoint> list = waypointsByDimension.get(dimension);
        if (list != null) {
            removed = list.removeIf(w -> w.x() == x && w.y() == y && w.z() == z);
        }
        if (removed) persist(level);
        return removed;
    }

    /**
     * Removes a waypoint locally without persisting.
     */
    public boolean removeLocal(ResourceKey<Level> dimension, int x, int y, int z) {
        List<Waypoint> list = waypointsByDimension.get(dimension);
        return list != null && list.removeIf(w -> w.x() == x && w.y() == y && w.z() == z);
    }

    /**
     * Clears all waypoints for the given dimension and persists.
     */
    public void clearWaypoints(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        waypointsByDimension.remove(dimension);
        WaypointSavedData.get(level).clearWaypoints();
    }

    /**
     * Clears local waypoints for the given dimension without persisting.
     */
    public void clearLocal(ResourceKey<Level> dimension) {
        waypointsByDimension.remove(dimension);
    }

    /**
     * Applies a synced waypoint list from a network packet.
     */
    public void applySync(ResourceKey<Level> dimension, CompoundTag tag) {
        var waypoints = new ArrayList<Waypoint>();
        var list = tag.getListOrEmpty("Waypoints");
        for (int i = 0; i < list.size(); i++) {
            waypoints.add(Waypoint.fromNbt(list.getCompoundOrEmpty(i)));
        }
        waypointsByDimension.put(dimension, waypoints);
    }

    /**
     * Syncs all waypoints to the given player.
     */
    public void syncToPlayer(ServerPlayer player) {
        Common.getSavedDataManager().syncToPlayer(player);
    }

    /**
     * Syncs all waypoints to all players in the given dimension.
     */
    public void syncToDimension(ServerLevel level) {
        Common.getSavedDataManager().syncToDimension(level);
    }

    /**
     * Loads waypoints when a level is loaded.
     */
    public void onLevelLoad(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            loadFromLevel(serverLevel);
        }
    }

    /**
     * Persists all waypoints when the server stops.
     */
    public void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ResourceKey<Level> dimension = level.dimension();
            List<Waypoint> list = waypointsByDimension.get(dimension);
            if (list != null) {
                WaypointSavedData.get(level).setData(toNbt(list));
            }
        }
    }

    /**
     * Serializes a list of waypoints to NBT.
     */
    public CompoundTag toNbt(List<Waypoint> waypoints) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Waypoint w : waypoints) {
            list.add(w.toNbt());
        }
        tag.put("Waypoints", list);
        return tag;
    }

    /**
     * Serializes waypoints for the given level to NBT.
     */
    public CompoundTag toNbt(ServerLevel level) {
        ResourceKey<Level> dim = level.dimension();
        if (!isLoaded(dim)) {
            loadFromLevel(level);
        }
        return toNbt(getWaypoints(dim));
    }

    private void persist(ServerLevel level) {
        WaypointSavedData.get(level).setData(toNbt(level));
    }
}
