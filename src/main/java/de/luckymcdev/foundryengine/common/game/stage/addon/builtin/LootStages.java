package de.luckymcdev.foundryengine.common.game.stage.addon.builtin;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.game.stage.addon.StageAddon;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class LootStages extends StageAddon<ResourceKey<LootTable>> {


    @Override
    protected String getObjectType() {
        return "loot table";
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        event.getEntity().getLootTable().ifPresent(lootTable -> {
            if (isAccessible(lootTable)) {
                return;
            }

            if (!canAccess(player, lootTable)) {
                event.getDrops().clear();
                player.sendSystemMessage(getMissingStagesMessage(player, lootTable));
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());

        if (be instanceof RandomizableContainerBlockEntity container) {
            ResourceKey<LootTable> lootTable = container.getLootTable();
            Common.LOGGER.info("randomizable block entity!");

            if (lootTable == null || isAccessible(lootTable)) {
                return;
            }

            Player player = event.getEntity();
            if (!canAccess(player, lootTable)) {
                event.setCanceled(true);
                player.sendSystemMessage(getMissingStagesMessage(player, lootTable));
            }
        }
    }
}