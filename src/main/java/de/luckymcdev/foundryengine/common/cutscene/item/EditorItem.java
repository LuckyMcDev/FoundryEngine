package de.luckymcdev.foundryengine.common.cutscene.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class EditorItem extends Item {
    public EditorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.literal("Hold right click to edit cutscenes."));
        consumer.accept(Component.literal("Hover nodes to select, scroll to push/pull."));
        consumer.accept(Component.literal("Quick click on an endpoint to add a node; crouch+click to delete."));
        consumer.accept(Component.literal("Use /cutscene to manage and play cutscenes."));
    }
}
