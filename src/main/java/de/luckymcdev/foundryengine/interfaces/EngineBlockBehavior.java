package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Configures block behavior properties such as collision, resistance, sounds, and friction.
 */
public interface EngineBlockBehavior extends EngineInterface<BlockBehaviour> {
	/**
	 * Sets whether the block has collision.
	 */
	void engine$hasCollision(boolean collision);

	/**
	 * Sets the explosion resistance of the block.
	 */
	void engine$setExplosionResistance(float resistance);

	/**
	 * Sets whether the block randomly ticks.
	 */
	void engine$setIsRandomlyTicking(boolean randomlyTicking);

	/**
	 * Sets the sound type for the block.
	 */
	void engine$setSoundType(SoundType type);

	/**
	 * Sets the friction coefficient of the block.
	 */
	void engine$setFriction(float friction);

	/**
	 * Sets the speed factor applied to entities on this block.
	 */
	void engine$setSpeedFactor(float speedFactor);

	/**
	 * Sets the jump factor applied to entities on this block.
	 */
	void engine$setJumpFactor(float jumpFactor);
}
