package de.luckymcdev.foundryengine.interfaces.level;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.server.level.ServerLevel;

/**
 * Controls server level ticking behavior when no players are present.
 */
public interface EngineLevelAccess extends EngineInterface<ServerLevel> {
	/**
	 * Sets whether the level should continue ticking when empty of players.
	 */
	default void engine$setTickWhenEmpty(boolean tickWhenEmpty) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns whether the level should currently tick.
	 */
	default boolean engine$shouldTick() {
		throw new NoMixinException(this);
	}
}
