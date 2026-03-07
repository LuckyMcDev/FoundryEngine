package io.github.luckymcdev.foundryengine.common.game.stage;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface StageAdditionPredicate {
    boolean test(Player player);
}