package de.luckymcdev.foundryengine.game.common.stage;

import net.minecraft.world.entity.player.Player;

/**
 * A simple condition for a player, to check things easily.
 */
@FunctionalInterface
public interface StageAdditionCondition {
	boolean test(Player player);
}