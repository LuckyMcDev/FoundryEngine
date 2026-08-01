package de.luckymcdev.foundryengine.common.game.stage.addon.builtin;

import de.luckymcdev.foundryengine.common.game.stage.addon.StageAddon;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;

public class MobStages extends StageAddon<EntityType<?>> {
	private final GenericRegistry<EntityType<?>, EntityType<?>> replacements = new GenericRegistry<>();
	private final Set<EntityType<?>> bypassSpawners = new HashSet<>();
	private final GenericRegistry<EntityType<?>, Integer> spawnRanges = new GenericRegistry<>();

	@Override
	protected String getObjectType() {
		return "entity";
	}

	public void addReplacement(EntityType<?> from, EntityType<?> to) {
		replacements.register(from, to);
	}

	public void setBypassSpawner(EntityType<?> type, boolean bypass) {
		if (bypass) {
			bypassSpawners.add(type);
		} else {
			bypassSpawners.remove(type);
		}
	}

	public void setSpawnRange(EntityType<?> type, int range) {
		spawnRanges.register(type, range);
	}

	private int getSpawnRange(EntityType<?> type) {
		var range = spawnRanges.get(type);
		return range != null ? range : 64;
	}

	private boolean hasNearbyPlayerWithStage(ServerLevel level, BlockPos pos, EntityType<?> type) {
		int range = getSpawnRange(type);
		double rangeSq = range * range;
		return level.players().stream()
			.filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < rangeSq)
			.anyMatch(p -> canAccess(p, type));
	}

	private boolean isLevelBlocked(ServerLevelAccessor levelAccessor, BlockPos pos, EntityType<?> type) {
		if (!(levelAccessor instanceof ServerLevel serverLevel)) {
			return false;
		}
		if (bypassSpawners.contains(type)) {
			return false;
		}
		return !hasNearbyPlayerWithStage(serverLevel, pos, type);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onMobSpawnPositionCheck(MobSpawnEvent.PositionCheck event) {
		var mob = event.getEntity();
		var type = mob.getType();

		if (isAccessible(type)) {
			return;
		}

		if (isLevelBlocked(event.getLevel(), new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ()), type)) {
			event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onMobSpawnerCheck(MobSpawnEvent.SpawnPlacementCheck event) {
		var type = event.getEntityType();

		if (isAccessible(type)) {
			return;
		}

		if (bypassSpawners.contains(type) && event.getSpawnType() == EntitySpawnReason.SPAWNER) {
			return;
		}

		if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
			event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
			return;
		}

		if (!hasNearbyPlayerWithStage(serverLevel, event.getPos(), type)) {
			event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityJoin(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof Mob mob)) {
			return;
		}

		var type = mob.getType();
		var replacement = replacements.get(type);
		if (replacement == null) {
			return;
		}

		if (isAccessible(type)) {
			return;
		}

		if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
			return;
		}

		var pos = mob.blockPosition();
		if (!hasNearbyPlayerWithStage(serverLevel, pos, type)) {
			event.setCanceled(true);
			replacement.spawn(serverLevel, pos, EntitySpawnReason.EVENT);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAttackEntity(AttackEntityEvent event) {
		var player = event.getEntity();
		var target = event.getTarget();

		if (!(target instanceof LivingEntity)) {
			return;
		}

		var type = target.getType();
		if (isAccessible(type)) {
			return;
		}
		if (canAccess(player, type)) {
			return;
		}

		event.setCanceled(true);
		player.sendSystemMessage(getMissingStagesMessage(player, type));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		var player = event.getEntity();
		var target = event.getTarget();

		if (!(target instanceof LivingEntity)) {
			return;
		}

		var type = target.getType();
		if (isAccessible(type)) {
			return;
		}
		if (canAccess(player, type)) {
			return;
		}

		event.setCanceled(true);
		player.sendSystemMessage(getMissingStagesMessage(player, type));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onMobTarget(LivingChangeTargetEvent event) {
		var entity = event.getEntity();
		var newTarget = event.getNewAboutToBeSetTarget();

		if (!(newTarget instanceof Player player)) {
			return;
		}

		var type = entity.getType();
		if (isAccessible(type)) {
			return;
		}
		if (canAccess(player, type)) {
			return;
		}

		event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onMobDespawn(MobDespawnEvent event) {
		var mob = event.getEntity();
		var type = mob.getType();

		if (isAccessible(type)) {
			return;
		}

		if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
			return;
		}

		// Only force the despawn when staged-player absence is the actual reason. The gating range
		// (spawnRange) only keeps staged players in mind, so check that the mob is genuinely out of
		// range of *every* player before overriding the vanilla result: if a (non-staged) player is
		// within the natural despawn radius, other despawn rules apply and we must defer to them.
		if (!hasNearbyPlayerWithStage(serverLevel, mob.blockPosition(), type)) {
			Player nearestPlayer = serverLevel.getNearestPlayer(mob, -1.0);
			if (nearestPlayer == null) {
				event.setResult(MobDespawnEvent.Result.ALLOW);
			} else {
				int despawnDistance = type.getCategory().getDespawnDistance();
				if (nearestPlayer.distanceToSqr(mob) >= (double) despawnDistance * despawnDistance) {
					event.setResult(MobDespawnEvent.Result.ALLOW);
				}
			}
		}
	}
}
