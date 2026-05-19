package de.luckymcdev.foundryengine.common.area;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class AreaManager {
    private final Map<ResourceKey<Level>, List<Area>> areasByDimension = new HashMap<>();
    private final Map<ResourceKey<Level>, Map<String, Set<UUID>>> lastMembersByDimension = new HashMap<>();

    private static CompoundTag areaToNbt(Area area) {
        return Area.writeToNbt(area);
    }

    public void loadFromLevel(ServerLevel level) {
        AreaSavedData savedData = AreaSavedData.get(level);
        ResourceKey<Level> dimension = level.dimension();
        areasByDimension.put(dimension, new ArrayList<>(savedData.getAreas()));
    }

    public void register(@Nullable ServerLevel level, Area area) {
        if (level != null) {
            AreaSavedData.get(level).addArea(area);
        }
        areasByDimension.computeIfAbsent(area.dimension(), k -> new ArrayList<>()).add(area);
    }

    public void update(@Nullable ServerLevel level, Area updatedArea) {
        if (level != null) {
            AreaSavedData.get(level).updateArea(updatedArea.id(), updatedArea);
        }
        List<Area> list = areasByDimension.get(updatedArea.dimension());
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).id().equals(updatedArea.id())) {
                    list.set(i, updatedArea);
                    break;
                }
            }
        }
    }

    public boolean isLoaded(ResourceKey<Level> dimension) {
        return areasByDimension.containsKey(dimension);
    }

    public void remove(@Nullable ServerLevel level, Area area) {
        if (level != null) {
            AreaSavedData.get(level).removeArea(area.id());
        }
        List<Area> list = areasByDimension.get(area.dimension());
        if (list != null) list.remove(area);
        var members = lastMembersByDimension.get(area.dimension());
        if (members != null) members.remove(area.id());
    }

    public List<Area> getAreasForDimension(ResourceKey<Level> dimension) {
        return areasByDimension.getOrDefault(dimension, Collections.emptyList());
    }

    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        ResourceKey<Level> dim = level.dimension();
        if (!isLoaded(dim)) {
            loadFromLevel(level);
        }

        List<Area> areas = getAreasForDimension(dim);
        if (areas.isEmpty()) {
            lastMembersByDimension.remove(dim);
            return;
        }

        Map<String, Set<UUID>> prevMembers = lastMembersByDimension.getOrDefault(dim, Collections.emptyMap());
        Map<String, Set<UUID>> currMembers = new HashMap<>();
        for (Area a : areas) {
            currMembers.put(a.id(), new HashSet<>());
        }

        Map<UUID, Entity> entitiesByUuid = new HashMap<>();
        level.getEntities().getAll().forEach(e -> entitiesByUuid.put(e.getUUID(), e));

        for (Entity entity : entitiesByUuid.values()) {
            var pos = entity.blockPosition();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            for (Area area : areas) {
                if (area.bounds().contains(x, y, z)) {
                    currMembers.get(area.id()).add(entity.getUUID());
                }
            }
        }

        for (Area area : areas) {
            Set<UUID> prev = prevMembers.getOrDefault(area.id(), Collections.emptySet());
            Set<UUID> curr = currMembers.getOrDefault(area.id(), Collections.emptySet());

            if (!curr.isEmpty()) {
                ArrayList<Entity> inside = new ArrayList<>(curr.size());
                for (UUID u : curr) {
                    Entity e = entitiesByUuid.get(u);
                    if (e != null) inside.add(e);
                }
                if (!inside.isEmpty()) {
                    NeoForge.EVENT_BUS.post(new AreaEvent.AreaTickEvent(area, inside));
                }
            }

            if (!curr.isEmpty()) {
                ArrayList<Entity> entering = new ArrayList<>();
                for (UUID u : curr) {
                    if (prev.contains(u)) continue;
                    Entity e = entitiesByUuid.get(u);
                    if (e != null) entering.add(e);
                }
                if (!entering.isEmpty()) {
                    NeoForge.EVENT_BUS.post(new AreaEvent.AreaEnterEvent(area, entering));
                }
            }

            if (!prev.isEmpty()) {
                ArrayList<Entity> leaving = new ArrayList<>();
                for (UUID u : prev) {
                    if (curr.contains(u)) continue;
                    Entity e = entitiesByUuid.get(u);
                    if (e != null) leaving.add(e);
                }
                if (!leaving.isEmpty()) {
                    NeoForge.EVENT_BUS.post(new AreaEvent.AreaLeaveEvent(area, leaving));
                }
            }
        }

        lastMembersByDimension.put(dim, currMembers);
    }

    public void applySync(ResourceKey<Level> dimension, CompoundTag tag) {
        areasByDimension.put(dimension, new ArrayList<>(AreaSavedData.makeList(tag)));
    }

    public CompoundTag toNbt(ServerLevel level) {
        if (!isLoaded(level.dimension())) {
            loadFromLevel(level);
        }
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Area area : getAreasForDimension(level.dimension())) {
            list.add(areaToNbt(area));
        }
        tag.put("Areas", list);
        return tag;
    }

    public void syncToPlayer(ServerPlayer player) {
        Common.getSavedDataManager().syncToPlayer(player);
    }

    public void syncToDimension(ServerLevel level) {
        Common.getSavedDataManager().syncToDimension(level);
    }

    public void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            loadFromLevel(level);
        }
    }

    public void onServerStopping(ServerStoppingEvent event) {
        // Save all areas when server is stopping
        for (ServerLevel level : event.getServer().getAllLevels()) {
            AreaSavedData savedData = AreaSavedData.get(level);
            savedData.setDirty(); // Ensure data is saved
        }
    }
}
