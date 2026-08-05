package de.luckymcdev.foundryengine.client.command;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ItemCommandManager {
	private static final GenericRegistry<ItemStack, ItemCommand> ITEM_COMMANDS = new GenericRegistry<>();

	public void register(ItemCommand command) {
		if (ITEM_COMMANDS.contains(command.stack())) {
			ITEM_COMMANDS.remove(command.stack());
			ITEM_COMMANDS.register(command.stack(), command);
		} else {
			ITEM_COMMANDS.register(command.stack(), command);
		}
	}

	public void remove(ItemStack stack) {
		ITEM_COMMANDS.remove(stack);
	}

	public void handleRightClick(PlayerInteractEvent.RightClickItem event) {
		if (event.getSide() != LogicalSide.CLIENT) {
			return;
		}
		ItemStack used = event.getItemStack();
		for (ItemCommand command : ITEM_COMMANDS.values()) {
			if (command.matches(used)) {
				command.execute((LocalPlayer) event.getEntity());
				return;
			}
		}
	}

	public void handleItemTooltip(ItemTooltipEvent event) {
		ItemStack hovered = event.getItemStack();
		for (ItemCommand command : ITEM_COMMANDS.values()) {
			if (command.matches(hovered)) {
				event.getToolTip().add(
					Component.literal("Executes Command: " + command.command()).withStyle(ChatFormatting.GRAY)
				);
				var stack = event.getItemStack();
				stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, oldData -> {
					CompoundTag copyTag = oldData.copyTag();
					var tag = new CompoundTag();
					tag.putString("command", command.command());
					copyTag.put(Common.MODID, tag);
					return CustomData.of(copyTag);
				});
				return;
			}
		}
	}

	public record ItemCommand(ItemStack stack, String command) {
		public void execute(LocalPlayer player) {
			player.connection.sendCommand(command());
		}

		public boolean matches(ItemStack other) {
			return ItemStack.isSameItemSameComponents(stack, other);
		}
	}
}
