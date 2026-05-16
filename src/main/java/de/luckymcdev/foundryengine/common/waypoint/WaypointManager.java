package de.luckymcdev.foundryengine.common.waypoint;

import de.luckymcdev.foundryengine.common.waypoint.storage.WaypointSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;

public class WaypointManager {
    private final Map<ResourceKey<Level>, List<Waypoint>> waypointsByDimension = new HashMap<>();

    public boolean isLoaded(ResourceKey<Level> dimension) {
        return waypointsByDimension.containsKey(dimension);
    }

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

    public List<Waypoint> getWaypoints(ResourceKey<Level> dimension) {
        return waypointsByDimension.getOrDefault(dimension, Collections.emptyList());
    }

    public void addWaypoint(ServerLevel level, Waypoint waypoint) {
        ResourceKey<Level> dimension = level.dimension();
        waypointsByDimension.computeIfAbsent(dimension, k -> new ArrayList<>()).add(waypoint);
        persist(level);
    }

    public void addLocal(ResourceKey<Level> dimension, Waypoint waypoint) {
        waypointsByDimension.computeIfAbsent(dimension, k -> new ArrayList<>()).add(waypoint);
    }

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

    public boolean removeLocal(ResourceKey<Level> dimension, int x, int y, int z) {
        List<Waypoint> list = waypointsByDimension.get(dimension);
        return list != null && list.removeIf(w -> w.x() == x && w.y() == y && w.z() == z);
    }

    public void clearWaypoints(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        waypointsByDimension.remove(dimension);
        WaypointSavedData.get(level).clearWaypoints();
    }

    public void clearLocal(ResourceKey<Level> dimension) {
        waypointsByDimension.remove(dimension);
    }

    public void replaceAll(ResourceKey<Level> dimension, List<Waypoint> waypoints) {
        waypointsByDimension.put(dimension, new ArrayList<>(waypoints));
    }

    public void onLevelLoad(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            loadFromLevel(serverLevel);
        }
    }

    public void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ResourceKey<Level> dimension = level.dimension();
            List<Waypoint> list = waypointsByDimension.get(dimension);
            if (list != null) {
                WaypointSavedData.get(level).setData(toNbt(list));
            }
        }
    }

    public CompoundTag toNbt(List<Waypoint> waypoints) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Waypoint w : waypoints) {
            list.add(w.toNbt());
        }
        tag.put("Waypoints", list);
        return tag;
    }

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
