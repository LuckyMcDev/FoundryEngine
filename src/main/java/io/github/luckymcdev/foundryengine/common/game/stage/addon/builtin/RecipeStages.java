package io.github.luckymcdev.foundryengine.common.game.stage.addon.builtin;

import io.github.luckymcdev.foundryengine.common.game.stage.addon.StageAddon;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// TODO
public class RecipeStages extends StageAddon<Identifier> {

    @Override
    protected String getObjectType() {
        return "Recipe";
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemSmelted(PlayerInteractEvent.ItemSmeltedEvent event) {

    }
}