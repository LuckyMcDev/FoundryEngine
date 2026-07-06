package de.luckymcdev.foundryengine.common.game.stage.addon.builtin;

import de.luckymcdev.foundryengine.common.game.stage.addon.StageAddon;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

public class DimensionStages extends StageAddon<ResourceKey<Level>> {

	@Override
	protected String getObjectType() {
		return "dimension";
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDimensionChange(EntityTravelToDimensionEvent event) {
		Entity entity = event.getEntity();

		if (!(entity instanceof Player player)) {
			return;
		}

		ResourceKey<Level> destination = event.getDimension();

		if (!canAccess(player, destination)) {
			event.setCanceled(true);
			player.sendOverlayMessage(getMissingStagesMessage(player, destination));
		}
	}
}