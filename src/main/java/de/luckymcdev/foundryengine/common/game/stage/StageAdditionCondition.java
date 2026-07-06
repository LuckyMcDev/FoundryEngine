package de.luckymcdev.foundryengine.common.game.stage;

import net.minecraft.world.entity.player.Player;

/**
 * A simple condition for a player, to check things easily.
 */
@FunctionalInterface
public interface StageAdditionCondition {
	boolean test(Player player);
}