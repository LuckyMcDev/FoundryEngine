package io.github.luckymcdev.foundryengine.common.game.stage;

import net.minecraft.world.entity.player.Player;

/**
 * A simple predicate of a player, to check things easily.
 */
@FunctionalInterface
public interface StageAdditionPredicate {
    boolean test(Player player);
}