package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.server.level.ServerLevel;

/**
 * Controls server level ticking behavior when no players are present.
 */
public interface EngineLevelAccess extends EngineInterface<ServerLevel> {
    /**
     * Sets whether the level should continue ticking when empty of players.
     */
    void engine$setTickWhenEmpty(boolean tickWhenEmpty);

    /**
     * Returns whether the level should currently tick.
     */
    boolean engine$shouldTick();
}
