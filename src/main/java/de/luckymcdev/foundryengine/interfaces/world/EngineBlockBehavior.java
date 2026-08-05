package de.luckymcdev.foundryengine.interfaces.world;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Configures block behavior properties such as collision, resistance, sounds, and friction.
 */
public interface EngineBlockBehavior extends EngineInterface<BlockBehaviour> {
	/**
	 * Sets whether the block has collision.
	 */
	default void engine$hasCollision(boolean collision) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets the explosion resistance of the block.
	 */
	default void engine$setExplosionResistance(float resistance) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets whether the block randomly ticks.
	 */
	default void engine$setIsRandomlyTicking(boolean randomlyTicking) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets the sound type for the block.
	 */
	default void engine$setSoundType(SoundType type) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets the friction coefficient of the block.
	 */
	default void engine$setFriction(float friction) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets the speed factor applied to entities on this block.
	 */
	default void engine$setSpeedFactor(float speedFactor) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets the jump factor applied to entities on this block.
	 */
	default void engine$setJumpFactor(float jumpFactor) {
		throw new NoMixinException(this);
	}
}
