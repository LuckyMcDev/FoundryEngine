package de.luckymcdev.foundryengine.common.game.stage.addon.builtin;

import de.luckymcdev.foundryengine.common.game.stage.addon.StageAddon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public class BlockStages extends StageAddon<Block> {

	@Override
	protected String getObjectType() {
		return "block";
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
		var player = event.getEntity();
		var state = event.getLevel().getBlockState(event.getPos());
		var block = state.getBlock();

		if (isAccessible(block)) {
			return;
		}
		if (canAccess(player, block)) {
			return;
		}

		event.setCanceled(true);
		player.sendSystemMessage(getMissingStagesMessage(player, block));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBreakBlock(BreakBlockEvent event) {
		var player = event.getPlayer();
		var block = event.getState().getBlock();

		if (isAccessible(block)) {
			return;
		}
		if (canAccess(player, block)) {
			return;
		}

		event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onHarvestDrops(BlockDropsEvent event) {
		if (!(event.getBreaker() instanceof Player player)) {
			return;
		}

		var block = event.getState().getBlock();
		if (isAccessible(block)) {
			return;
		}
		if (canAccess(player, block)) {
			return;
		}

		event.getDrops().clear();
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onHarvestCheck(PlayerEvent.HarvestCheck event) {
		var block = event.getTargetBlock().getBlock();
		if (isAccessible(block)) {
			return;
		}
		if (canAccess(event.getEntity(), block)) {
			return;
		}

		event.setCanHarvest(false);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		var block = event.getState().getBlock();
		if (isAccessible(block)) {
			return;
		}
		if (canAccess(event.getEntity(), block)) {
			return;
		}

		event.setNewSpeed(0.0f);
	}
}
