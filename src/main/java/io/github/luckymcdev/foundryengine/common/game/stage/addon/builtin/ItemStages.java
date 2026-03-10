package io.github.luckymcdev.foundryengine.common.game.stage.addon.builtin;

import io.github.luckymcdev.foundryengine.common.game.stage.addon.StageAddon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Set;

public class ItemStages extends StageAddon<Item> {


    @Override
    protected String getObjectType() {
        return "item";
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onItemUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (!canAccess(player, stack.getItem())) {
            event.setCanceled(true);
            player.displayClientMessage(getMissingStagesMessage(player, stack.getItem()), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onItemUseOnBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (!canAccess(player, stack.getItem())) {
            event.setCanceled(true);
            player.displayClientMessage(getMissingStagesMessage(player, stack.getItem()), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onItemUseOnEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (!canAccess(player, stack.getItem())) {
            event.setCanceled(true);
            player.displayClientMessage(getMissingStagesMessage(player, stack.getItem()), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getCrafting();

        if (!canAccess(player, stack.getItem())) {
            stack.setCount(0);
            player.displayClientMessage(getMissingStagesMessage(player, stack.getItem()), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getSmelting();

        if (!canAccess(player, stack.getItem())) {
            stack.setCount(0);
            player.displayClientMessage(getMissingStagesMessage(player, stack.getItem()), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        if (player == null) return;

        Item item = stack.getItem();
        if (isAccessible(item)) return;

        Set<String> required = getRequiredStages(item);
        Set<String> missing = getMissingStages(player, item);

        if (!required.isEmpty() && !missing.isEmpty()) {
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.literal("Required Stages:").withStyle(ChatFormatting.GOLD));

            for (String stage : required) {
                ChatFormatting color = !missing.contains(stage) ? ChatFormatting.GREEN : ChatFormatting.RED;
                event.getToolTip().add(Component.literal(stage).withStyle(color));
            }
        }
    }
}