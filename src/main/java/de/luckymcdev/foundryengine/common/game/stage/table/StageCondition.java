package de.luckymcdev.foundryengine.common.game.stage.table;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface StageCondition {
	boolean test(Player player);
}
