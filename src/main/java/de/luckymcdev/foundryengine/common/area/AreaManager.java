package de.luckymcdev.foundryengine.common.area;

import de.luckymcdev.foundryengine.common.area.module.AreaBlockModule;
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule;
import de.luckymcdev.foundryengine.common.area.module.AreaLeaveModule;
import de.luckymcdev.foundryengine.common.area.module.AreaModule;
import de.luckymcdev.foundryengine.common.area.module.AreaTickModule;
import de.luckymcdev.foundryengine.common.area.preset.AreaPreset;
import de.luckymcdev.foundryengine.common.savedata.SavedDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AreaManager {
	public static final String SAVE_SECTION = "areas";
	private static final int SPATIAL_CELL_SIZE = 32;
	private final SavedDataManager savedDataManager;
	private final Map<Identifier, AreaModule> moduleTypes = new HashMap<>();
	private final Map<String, AreaPreset> presets = new HashMap<>();
	private final Map<Identifier, Area> areasById = new HashMap<>();
	private final Map<ResourceKey<Level>, List<Identifier>> areaIdsByDimension = new HashMap<>();
	private final Map<ResourceKey<Level>, Map<Identifier, Set<UUID>>> lastMembersByDimension = new HashMap<>();
	private final Map<ResourceKey<Level>, Map<Long, List<Area>>> areaSpatialIndex = new HashMap<>();

	public AreaManager(SavedDataManager savedDataManager) {
		this.savedDataManager = savedDataManager;
	}

	private static long spatialCellKey(int cellX, int cellZ) {
		return ((long) cellX << 32) | (cellZ & 0xFFFFFFFFL);
	}

	public void registerModuleType(AreaModule module) {
		moduleTypes.put(module.id(), module);
	}

	@Nullable
	public AreaModule getModuleType(Identifier id) {
		return moduleTypes.get(id);
	}

	public Collection<AreaModule> getRegisteredModuleTypes() {
		return Collections.unmodifiableCollection(moduleTypes.values());
	}

	public void registerPreset(AreaPreset preset) {
		presets.put(preset.id(), preset);
	}

	@Nullable
	public AreaPreset getPreset(String id) {
		return presets.get(id);
	}

	public Collection<AreaPreset> getPresets() {
		return Collections.unmodifiableCollection(presets.values());
	}

	@Nullable
	public Area getArea(Identifier id) {
		return areasById.get(id);
	}

	public List<Area> getAreasForDimension(ResourceKey<Level> dimension) {
		List<Identifier> ids = areaIdsByDimension.getOrDefault(dimension, Collections.emptyList());
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}
		List<Area> result = new ArrayList<>(ids.size());
		for (Identifier aid : ids) {
			Area a = areasById.get(aid);
			if (a != null) {
				result.add(a);
			}
		}
		return result;
	}

	public boolean isLoaded(ResourceKey<Level> dimension) {
		return areaIdsByDimension.containsKey(dimension);
	}

	public void register(@Nullable ServerLevel level, Area area) {
		areasById.put(area.id(), area);
		var dimIds = areaIdsByDimension.computeIfAbsent(area.dimension(), k -> new ArrayList<>());
		if (!dimIds.contains(area.id())) {
			dimIds.add(area.id());
		}
		indexAreaSpatially(area);
		if (level != null) {
			save();
			syncToAll();
		}
	}

	public void update(@Nullable ServerLevel level, Area updatedArea) {
		areasById.put(updatedArea.id(), updatedArea);
		if (level != null) {
			save();
			syncToAll();
		}
	}

	public void remove(@Nullable ServerLevel level, Area area) {
		areasById.remove(area.id());
		List<Identifier> ids = areaIdsByDimension.get(area.dimension());
		if (ids != null) {
			ids.remove(area.id());
		}
		var members = lastMembersByDimension.get(area.dimension());
		if (members != null) {
			members.remove(area.id());
		}
		removeAreaFromSpatialIndex(area);
		if (level != null) {
			save();
			syncToAll();
		}
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		var allList = new ListTag();
		for (Area area : areasById.values()) {
			allList.add(area.writeToNbt());
		}
		tag.put("Areas", allList);
		return tag;
	}

	public void fromNbt(CompoundTag tag) {
		areasById.clear();
		areaIdsByDimension.clear();
		areaSpatialIndex.clear();
		lastMembersByDimension.clear();
		var list = tag.getListOrEmpty("Areas");
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof CompoundTag ct) {
				Area area = Area.readFromNbt(ct);
				areasById.put(area.id(), area);
				areaIdsByDimension.computeIfAbsent(area.dimension(), k -> new ArrayList<>()).add(area.id());
				indexAreaSpatially(area);
			}
		}
	}

	public void save() {
		savedDataManager.setSection(SAVE_SECTION, toNbt());
	}

	public void load() {
		fromNbt(savedDataManager.getSection(SAVE_SECTION));
	}

	public void syncToAll() {
		savedDataManager.syncToAll();
	}

	public void syncToPlayer(ServerPlayer player) {
		savedDataManager.syncToPlayer(player);
	}

	public void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}

		ResourceKey<Level> dim = level.dimension();
		if (!isLoaded(dim)) {
			return;
		}

		List<Area> areas = getAreasForDimension(dim);
		if (areas.isEmpty()) {
			lastMembersByDimension.remove(dim);
			return;
		}

		Map<Identifier, Set<UUID>> prevMembers = lastMembersByDimension.getOrDefault(dim, Collections.emptyMap());
		Map<Identifier, Set<UUID>> currMembers = new HashMap<>();
		for (Area a : areas) {
			currMembers.put(a.id(), new HashSet<>());
		}

		Map<Long, List<Area>> spatial = areaSpatialIndex.get(dim);
		Map<UUID, Entity> entitiesByUuid = new HashMap<>();
		level.getEntities().getAll().forEach(e -> entitiesByUuid.put(e.getUUID(), e));

		for (Entity entity : entitiesByUuid.values()) {
			var pos = entity.blockPosition();
			long cellKey = spatialCellKey(pos.getX() >> 5, pos.getZ() >> 5);
			List<Area> candidates = spatial != null ? spatial.getOrDefault(cellKey, List.of()) : areas;

			for (Area area : candidates) {
				if (area.bounds().contains(pos.getX(), pos.getY(), pos.getZ())) {
					currMembers.get(area.id()).add(entity.getUUID());
				}
			}
		}

		for (Area area : areas) {
			Set<UUID> prev = prevMembers.getOrDefault(area.id(), Collections.emptySet());
			Set<UUID> curr = currMembers.getOrDefault(area.id(), Collections.emptySet());

			if (!curr.isEmpty()) {
				dispatchTick(area, level);
				for (UUID u : curr) {
					if (prev.contains(u)) {
						continue;
					}
					Entity e = entitiesByUuid.get(u);
					if (e instanceof ServerPlayer player) {
						dispatchEnter(area, player);
					}
				}
			}

			if (!prev.isEmpty()) {
				for (UUID u : prev) {
					if (curr.contains(u)) {
						continue;
					}
					Entity e = entitiesByUuid.get(u);
					if (e instanceof ServerPlayer player) {
						dispatchLeave(area, player);
					}
				}
			}
		}

		lastMembersByDimension.put(dim, currMembers);
	}

	private void indexAreaSpatially(Area area) {
		var bounds = area.bounds();
		int minCellX = (int) Math.floor(bounds.minX) >> 5;
		int maxCellX = (int) Math.floor(bounds.maxX) >> 5;
		int minCellZ = (int) Math.floor(bounds.minZ) >> 5;
		int maxCellZ = (int) Math.floor(bounds.maxZ) >> 5;
		var grid = areaSpatialIndex.computeIfAbsent(area.dimension(), k -> new HashMap<>());
		for (int cx = minCellX; cx <= maxCellX; cx++) {
			for (int cz = minCellZ; cz <= maxCellZ; cz++) {
				grid.computeIfAbsent(spatialCellKey(cx, cz), k -> new ArrayList<>()).add(area);
			}
		}
	}

	private void removeAreaFromSpatialIndex(Area area) {
		var grid = areaSpatialIndex.get(area.dimension());
		if (grid == null) {
			return;
		}
		grid.values().forEach(list -> list.remove(area));
	}

	private void dispatchTick(Area area, ServerLevel level) {
		for (Identifier mid : area.moduleIds()) {
			AreaModule module = moduleTypes.get(mid);
			if (module instanceof AreaTickModule tick) {
				tick.tick(level, area);
			}
		}
	}

	private void dispatchEnter(Area area, ServerPlayer player) {
		for (Identifier mid : area.moduleIds()) {
			AreaModule module = moduleTypes.get(mid);
			if (module instanceof AreaEnterModule enter) {
				enter.onEnter(player, area);
			}
		}
	}

	private void dispatchLeave(Area area, ServerPlayer player) {
		for (Identifier mid : area.moduleIds()) {
			AreaModule module = moduleTypes.get(mid);
			if (module instanceof AreaLeaveModule leave) {
				leave.onLeave(player, area);
			}
		}
	}

	public void onBlockBreak(BreakBlockEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}
		if (!(event.getPlayer() instanceof ServerPlayer player)) {
			return;
		}
		BlockPos pos = event.getPos();
		BlockState state = event.getState();

		for (Area area : areasInDimension(level.dimension())) {
			if (!area.contains(pos)) {
				continue;
			}
			for (Identifier mid : area.moduleIds()) {
				AreaModule module = moduleTypes.get(mid);
				if (module instanceof AreaBlockModule block) {
					block.onBlockBreak(event, level, area, pos, state, player);
				}
			}
		}
	}

	public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}

		BlockPos pos = event.getPos();
		BlockState state = event.getState();

		for (Area area : areasInDimension(level.dimension())) {
			if (!area.contains(pos)) {
				continue;
			}
			for (Identifier mid : area.moduleIds()) {
				AreaModule module = moduleTypes.get(mid);
				if (module instanceof AreaBlockModule block) {
					block.onBlockPlace(event, level, area, pos, state, player);
				}
			}
		}
	}

	private List<Area> areasInDimension(ResourceKey<Level> dimension) {
		List<Identifier> ids = areaIdsByDimension.getOrDefault(dimension, Collections.emptyList());
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}
		List<Area> result = new ArrayList<>(ids.size());
		for (Identifier aid : ids) {
			Area a = areasById.get(aid);
			if (a != null) {
				result.add(a);
			}
		}
		return result;
	}
}
