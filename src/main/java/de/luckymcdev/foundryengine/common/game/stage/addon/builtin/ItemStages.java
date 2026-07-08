package de.luckymcdev.foundryengine.common.game.stage.addon.builtin;

import de.luckymcdev.foundryengine.common.game.stage.addon.StageAddon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;

public class ItemStages extends StageAddon<Item> {
	private static final int SCAN_INTERVAL = 20;
	private final Set<Item> trackedItems = new HashSet<>();

	@Override
	protected String getObjectType() {
		return "item";
	}

	@Override
	public void requireStages(Item object, Identifier... stages) {
		super.requireStages(object, stages);
		trackedItems.add(object);
	}

	@Override
	public void requireStages(Item object, Component message, Identifier... stages) {
		super.requireStages(object, message, stages);
		trackedItems.add(object);
	}

	@Override
	public void clear(Item object) {
		super.clear(object);
		trackedItems.remove(object);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onPlayerTick(PlayerTickEvent.Post event) {
		var player = event.getEntity();
		if (player.level().isClientSide()) {
			return;
		}
		if (player.level().getGameTime() % SCAN_INTERVAL != 0) {
			return;
		}

		var inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			var stack = inventory.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			if (!trackedItems.contains(stack.getItem())) {
				continue;
			}
			if (canAccess(player, stack.getItem())) {
				continue;
			}

			var drop = inventory.removeItem(slot, stack.getCount());
			if (!drop.isEmpty()) {
				var itemEntity = new ItemEntity(
					player.level(), player.getX(), player.getY(), player.getZ(),
					drop
				);
				player.level().addFreshEntity(itemEntity);
				player.sendSystemMessage(getMissingStagesMessage(player, stack.getItem()));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemPickup(ItemEntityPickupEvent.Pre event) {
		var player = event.getPlayer();
		var stack = event.getItemEntity().getItem();
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();
		if (!trackedItems.contains(item)) {
			return;
		}
		if (canAccess(player, item)) {
			return;
		}

		event.setCanPickup(TriState.FALSE);
		player.sendSystemMessage(getMissingStagesMessage(player, item));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		var player = event.getEntity();
		var stack = event.getItemStack();
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();
		if (!trackedItems.contains(item)) {
			return;
		}
		if (canAccess(player, item)) {
			return;
		}

		event.setCanceled(true);
		player.sendSystemMessage(getMissingStagesMessage(player, item));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemUseOnBlock(PlayerInteractEvent.RightClickBlock event) {
		var player = event.getEntity();
		var stack = event.getItemStack();
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();
		if (!trackedItems.contains(item)) {
			return;
		}
		if (canAccess(player, item)) {
			return;
		}

		event.setCanceled(true);
		player.sendSystemMessage(getMissingStagesMessage(player, item));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemUseOnEntity(PlayerInteractEvent.EntityInteract event) {
		var player = event.getEntity();
		var stack = event.getItemStack();
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();
		if (!trackedItems.contains(item)) {
			return;
		}
		if (canAccess(player, item)) {
			return;
		}

		event.setCanceled(true);
		player.sendSystemMessage(getMissingStagesMessage(player, item));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemUseOnEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		var player = event.getEntity();
		var stack = event.getItemStack();
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();
		if (!trackedItems.contains(item)) {
			return;
		}
		if (canAccess(player, item)) {
			return;
		}

		event.setCanceled(true);
		player.sendSystemMessage(getMissingStagesMessage(player, item));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAttackEntity(AttackEntityEvent event) {
		var player = event.getEntity();
		var stack = player.getMainHandItem();
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();
		if (!trackedItems.contains(item)) {
			return;
		}
		if (canAccess(player, item)) {
			return;
		}

		event.setCanceled(true);
		player.sendSystemMessage(getMissingStagesMessage(player, item));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
		var player = event.getEntity();
		var stack = event.getCrafting();
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();
		if (!trackedItems.contains(item)) {
			return;
		}
		if (canAccess(player, item)) {
			return;
		}

		stack.setCount(0);
		player.sendSystemMessage(getMissingStagesMessage(player, item));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
		var player = event.getEntity();
		var stack = event.getSmelting();
		if (stack.isEmpty()) {
			return;
		}

		var item = stack.getItem();
		if (!trackedItems.contains(item)) {
			return;
		}
		if (canAccess(player, item)) {
			return;
		}

		stack.setCount(0);
		player.sendSystemMessage(getMissingStagesMessage(player, item));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onItemTooltip(ItemTooltipEvent event) {
		var player = event.getEntity();
		if (player == null) {
			return;
		}

		var item = event.getItemStack().getItem();
		if (!trackedItems.contains(item)) {
			return;
		}
		if (isAccessible(item)) {
			return;
		}

		var required = getRequiredStages(item);
		var missing = getMissingStages(player, item);

		if (!required.isEmpty() && !missing.isEmpty()) {
			event.getToolTip().add(Component.empty());
			event.getToolTip().add(Component.translatable("foundryengine.stage.required_header").withStyle(ChatFormatting.GOLD));

			for (var stage : required) {
				var color = !missing.contains(stage) ? ChatFormatting.GREEN : ChatFormatting.RED;
				event.getToolTip().add(Component.literal(stage.toString()).withStyle(color));
			}
		}
	}
}
