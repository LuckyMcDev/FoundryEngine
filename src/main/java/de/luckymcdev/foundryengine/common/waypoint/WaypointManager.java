package de.luckymcdev.foundryengine.common.waypoint;

import de.luckymcdev.foundryengine.common.savedata.SavedDataManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaypointManager {
	public static final String SAVE_SECTION = "waypoints";
	private final SavedDataManager savedDataManager;
	private final Map<ResourceKey<Level>, List<Waypoint>> waypointsByDimension = new HashMap<>();

	public WaypointManager(SavedDataManager savedDataManager) {
		this.savedDataManager = savedDataManager;
	}

	public boolean isLoaded(ResourceKey<Level> dimension) {
		return waypointsByDimension.containsKey(dimension);
	}

	public List<Waypoint> getWaypoints(ResourceKey<Level> dimension) {
		return waypointsByDimension.getOrDefault(dimension, Collections.emptyList());
	}

	public void addWaypoint(ServerLevel level, Waypoint waypoint) {
		ResourceKey<Level> dimension = level.dimension();
		waypointsByDimension.computeIfAbsent(dimension, k -> new ArrayList<>()).add(waypoint);
		save();
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
		if (removed) {
			save();
		}
		return removed;
	}

	public boolean removeLocal(ResourceKey<Level> dimension, int x, int y, int z) {
		List<Waypoint> list = waypointsByDimension.get(dimension);
		return list != null && list.removeIf(w -> w.x() == x && w.y() == y && w.z() == z);
	}

	public void clearWaypoints(ServerLevel level) {
		waypointsByDimension.remove(level.dimension());
		save();
	}

	public void clearLocal(ResourceKey<Level> dimension) {
		waypointsByDimension.remove(dimension);
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		for (var entry : waypointsByDimension.entrySet()) {
			var dimTag = new CompoundTag();
			var list = new ListTag();
			for (var w : entry.getValue()) {
				list.add(w.toNbt());
			}
			dimTag.put("Waypoints", list);
			tag.put(entry.getKey().identifier().toString(), dimTag);
		}
		return tag;
	}

	public void fromNbt(CompoundTag tag) {
		waypointsByDimension.clear();
		for (var dimKey : tag.keySet()) {
			var dimTag = tag.getCompoundOrEmpty(dimKey);
			var dim = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimKey));
			var waypoints = new ArrayList<Waypoint>();
			var list = dimTag.getListOrEmpty("Waypoints");
			for (int i = 0; i < list.size(); i++) {
				waypoints.add(Waypoint.fromNbt(list.getCompoundOrEmpty(i)));
			}
			waypointsByDimension.put(dim, waypoints);
		}
	}

	public void syncToAll() {
		savedDataManager.syncToAll();
	}

	public void save() {
		savedDataManager.setSection(SAVE_SECTION, toNbt());
	}

	public void load() {
		fromNbt(savedDataManager.getSection(SAVE_SECTION));
	}
}
