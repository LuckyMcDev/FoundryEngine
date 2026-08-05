package de.luckymcdev.foundryengine.interfaces.world;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Configures block state behavior properties such as light emission, destroy speed, and tool requirements.
 */
public interface EngineBlockStateBehavior extends EngineInterface<BlockBehaviour.BlockStateBase> {
	/**
	 * Sets the light emission level for this block state.
	 */
	default void engine$setLightEmission(int emission) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets the destroy speed of this block state.
	 */
	default void engine$setDestroySpeed(float speed) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets whether this block state requires a tool to drop.
	 */
	default void engine$setRequiresTool(boolean requiresTool) {
		throw new NoMixinException(this);
	}
}
