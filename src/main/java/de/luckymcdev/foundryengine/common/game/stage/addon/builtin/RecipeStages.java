package de.luckymcdev.foundryengine.common.game.stage.addon.builtin;

import de.luckymcdev.foundryengine.common.game.stage.addon.StageAddon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class RecipeStages extends StageAddon<Identifier> {

	@Override
	protected String getObjectType() {
		return "recipe";
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
		var stack = event.getCrafting();
		if (stack.isEmpty()) {
			return;
		}

		var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		if (isAccessible(itemId)) {
			return;
		}
		if (canAccess(event.getEntity(), itemId)) {
			return;
		}

		stack.setCount(0);
		event.getEntity().sendSystemMessage(getMissingStagesMessage(event.getEntity(), itemId));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
		var stack = event.getSmelting();
		if (stack.isEmpty()) {
			return;
		}

		var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		if (isAccessible(itemId)) {
			return;
		}
		if (canAccess(event.getEntity(), itemId)) {
			return;
		}

		stack.setCount(0);
		event.getEntity().sendSystemMessage(getMissingStagesMessage(event.getEntity(), itemId));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onItemTooltip(ItemTooltipEvent event) {
		var player = event.getEntity();
		if (player == null) {
			return;
		}

		var stack = event.getItemStack();
		var itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

		if (isAccessible(itemId)) {
			return;
		}
		if (canAccess(player, itemId)) {
			return;
		}

		var required = getRequiredStages(itemId);
		if (required.isEmpty()) {
			return;
		}

		event.getToolTip().add(Component.empty());
		event.getToolTip().add(Component.translatable("foundryengine.stage.required_for_crafting")
			.withStyle(ChatFormatting.GOLD));

		for (var stage : required) {
			event.getToolTip().add(Component.literal(stage.toString())
				.withStyle(ChatFormatting.RED));
		}
	}
}
