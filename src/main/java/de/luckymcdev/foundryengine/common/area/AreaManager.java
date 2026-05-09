package de.luckymcdev.foundryengine.common.area;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.ClientBoundAreaSyncPacket;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;

public class AreaManager {
    private final Map<ResourceKey<Level>, List<Area>> areasByDimension = new HashMap<>();
    private final Map<UUID, Set<String>> entityAreaMap = new HashMap<>();

    public void loadFromLevel(ServerLevel level) {
        AreaSavedData savedData = AreaSavedData.get(level);
        ResourceKey<Level> dimension = level.dimension();
        areasByDimension.put(dimension, new ArrayList<>(savedData.getAreas()));
    }

    public void register(ServerLevel level, Area area) {
        AreaSavedData.get(level).addArea(area);
        areasByDimension.computeIfAbsent(area.dimension(), k -> new ArrayList<>()).add(area);
    }

    public void remove(ServerLevel level, Area area) {
        AreaSavedData.get(level).removeArea(area.id());
        List<Area> list = areasByDimension.get(area.dimension());
        if (list != null) list.remove(area);
        entityAreaMap.values().forEach(set -> set.remove(area.id()));
    }

    public void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        GlobalPos globalPos = GlobalPos.of(entity.level().dimension(), entity.blockPosition());
        UUID uuid = entity.getUUID();

        List<Area> dimensionAreas = areasByDimension.getOrDefault(globalPos.dimension(), Collections.emptyList());

        Set<String> previousAreas = entityAreaMap.getOrDefault(uuid, Collections.emptySet());
        Set<String> currentAreas = new HashSet<>();

        for (Area area : dimensionAreas) {
            if (area.contains(globalPos)) {
                currentAreas.add(area.id());
            }
        }

        for (Area area : dimensionAreas) {
            String id = area.id();
            boolean wasInside = previousAreas.contains(id);
            boolean isInside = currentAreas.contains(id);

            if (isInside && !wasInside) {
                NeoForge.EVENT_BUS.post(new AreaEvent.AreaEnterEvent(area, List.of(entity)));
            } else if (!isInside && wasInside) {
                NeoForge.EVENT_BUS.post(new AreaEvent.AreaLeaveEvent(area, List.of(entity)));
            } else if (isInside) {
                NeoForge.EVENT_BUS.post(new AreaEvent.AreaTickEvent(area, List.of(entity)));
            }
        }

        if (currentAreas.isEmpty()) {
            entityAreaMap.remove(uuid);
        } else {
            entityAreaMap.put(uuid, currentAreas);
        }
    }

    public List<Area> getAreasForDimension(ResourceKey<Level> dimension) {
        return areasByDimension.getOrDefault(dimension, Collections.emptyList());
    }

    public void syncAreasToPlayer(ServerPlayer player) {
        ResourceKey<Level> dimension = player.level().dimension();
        List<Area> areas = getAreasForDimension(dimension);

        var packet = ClientBoundAreaSyncPacket.create(dimension.identifier(), areas);
        Common.getNetworkManager().sendToPlayer(packet, player);
    }

    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncAreasToPlayer(player);
        }
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

    public void onEntityRemoved(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        UUID uuid = entity.getUUID();
        Set<String> occupiedAreas = entityAreaMap.remove(uuid);
        if (occupiedAreas == null || occupiedAreas.isEmpty()) return;

        List<Area> dimensionAreas = areasByDimension.getOrDefault(entity.level().dimension(), Collections.emptyList());
        for (Area area : dimensionAreas) {
            if (occupiedAreas.contains(area.id())) {
                NeoForge.EVENT_BUS.post(new AreaEvent.AreaLeaveEvent(area, List.of(entity)));
            }
        }
    }
}