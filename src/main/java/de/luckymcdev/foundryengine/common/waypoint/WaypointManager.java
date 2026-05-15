package de.luckymcdev.foundryengine.common.waypoint;

import de.luckymcdev.foundryengine.common.waypoint.storage.WaypointSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;

public class WaypointManager {
    private final Map<ResourceKey<Level>, List<WaypointData>> waypointsByDimension = new HashMap<>();

    public void loadFromLevel(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        WaypointSavedData savedData = WaypointSavedData.get(level);
        List<WaypointData> loaded = new ArrayList<>();
        ListTag list = savedData.getData().getListOrEmpty("Waypoints");
        for (int i = 0; i < list.size(); i++) {
            loaded.add(WaypointData.fromNbt(list.getCompoundOrEmpty(i)));
        }
        waypointsByDimension.put(dimension, loaded);
    }

    public List<WaypointData> getWaypoints(ResourceKey<Level> dimension) {
        return waypointsByDimension.getOrDefault(dimension, Collections.emptyList());
    }

    public void addWaypoint(ServerLevel level, WaypointData waypoint) {
        ResourceKey<Level> dimension = level.dimension();
        WaypointSavedData.get(level).addWaypoint(waypoint.x(), waypoint.y(), waypoint.z(),
                waypoint.name(), waypoint.icon(), waypoint.color());
        waypointsByDimension.computeIfAbsent(dimension, k -> new ArrayList<>()).add(waypoint);
    }

    public void addLocal(ResourceKey<Level> dimension, WaypointData waypoint) {
        waypointsByDimension.computeIfAbsent(dimension, k -> new ArrayList<>()).add(waypoint);
    }

    public boolean removeWaypoint(ServerLevel level, int x, int y, int z) {
        ResourceKey<Level> dimension = level.dimension();
        boolean saved = WaypointSavedData.get(level).removeWaypoint(x, y, z);
        List<WaypointData> list = waypointsByDimension.get(dimension);
        if (list != null) {
            saved |= list.removeIf(w -> w.x() == x && w.y() == y && w.z() == z);
        }
        return saved;
    }

    public boolean removeLocal(ResourceKey<Level> dimension, int x, int y, int z) {
        List<WaypointData> list = waypointsByDimension.get(dimension);
        return list != null && list.removeIf(w -> w.x() == x && w.y() == y && w.z() == z);
    }

    public void clearWaypoints(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        WaypointSavedData.get(level).clearWaypoints();
        waypointsByDimension.remove(dimension);
    }

    public void clearLocal(ResourceKey<Level> dimension) {
        waypointsByDimension.remove(dimension);
    }

    public void replaceAll(ResourceKey<Level> dimension, List<WaypointData> waypoints) {
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
            List<WaypointData> list = waypointsByDimension.get(dimension);
            if (list != null) {
                WaypointSavedData.get(level).setData(toNbt(list));
            }
        }
    }

    private CompoundTag toNbt(List<WaypointData> waypoints) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (WaypointData w : waypoints) {
            list.add(w.toNbt());
        }
        tag.put("Waypoints", list);
        return tag;
    }
}
