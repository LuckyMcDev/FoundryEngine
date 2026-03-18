package io.github.luckymcdev.foundryengine.common.game.stage.addon.builtin;

import io.github.luckymcdev.foundryengine.common.game.stage.addon.StageAddon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class MobStages extends StageAddon<EntityType<?>> {


    @Override
    protected String getObjectType() {
        return "entity";
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMobSpawn(MobSpawnEvent.SpawnPlacementCheck event) {
        EntityType<?> type = event.getEntityType();

        if (isAccessible(type)) {
            return;
        }

        event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        if (!(target instanceof LivingEntity)) {
            return;
        }

        EntityType<?> type = target.getType();

        if (!canAccess(player, type)) {
            event.setCanceled(true);
            player.sendSystemMessage(getMissingStagesMessage(player, type));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        if (!(target instanceof LivingEntity)) {
            return;
        }

        EntityType<?> type = target.getType();

        if (!canAccess(player, type)) {
            event.setCanceled(true);
            player.sendSystemMessage(getMissingStagesMessage(player, type));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMobTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();

        if (!(newTarget instanceof Player player)) {
            return;
        }

        EntityType<?> type = entity.getType();

        if (!canAccess(player, type)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onMobTick(MobDespawnEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType<?> type = entity.getType();

        if (isAccessible(type)) {
            return;
        }

        boolean anyPlayerCanSee = entity.level().players().stream()
                .filter(player -> player.distanceToSqr(entity) < 64 * 64)
                .anyMatch(player -> canAccess(player, type));

        if (!anyPlayerCanSee) {
            event.setResult(MobDespawnEvent.Result.ALLOW);
        }
    }
}