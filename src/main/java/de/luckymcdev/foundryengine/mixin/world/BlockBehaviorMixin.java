package de.luckymcdev.foundryengine.mixin.world;

import de.luckymcdev.foundryengine.interfaces.EngineBlockBehavior;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Implements {@link EngineBlockBehavior} on BlockBehaviour to allow runtime property modification.
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviorMixin implements EngineBlockBehavior {
    @Shadow
    public float explosionResistance;
    @Shadow
    public boolean isRandomlyTicking;
    @Shadow
    public boolean hasCollision;
    @Shadow
    public SoundType soundType;
    @Shadow
    public float friction;
    @Shadow
    public float speedFactor;
    @Shadow
    public float jumpFactor;

    /**
     * Sets whether this block has collision.
     */
    @Override
    public void engine$hasCollision(boolean collision) {
        hasCollision = collision;
    }

    /**
     * Sets the explosion resistance of this block.
     */
    @Override
    public void engine$setExplosionResistance(float resistance) {
        explosionResistance = resistance;
    }

    /**
     * Sets whether this block randomly ticks.
     */
    @Override
    public void engine$setIsRandomlyTicking(boolean randomlyTicking) {
        isRandomlyTicking = randomlyTicking;
    }

    /**
     * Sets the sound type of this block.
     */
    @Override
    public void engine$setSoundType(SoundType type) {
        soundType = type;
    }

    /**
     * Sets the friction of this block.
     */
    @Override
    public void engine$setFriction(float friction) {
        this.friction = friction;
    }

    /**
     * Sets the speed factor of this block.
     */
    @Override
    public void engine$setSpeedFactor(float speedFactor) {
        this.speedFactor = speedFactor;
    }

    /**
     * Sets the jump factor of this block.
     */
    @Override
    public void engine$setJumpFactor(float nJumpFactor) {
        jumpFactor = nJumpFactor;
    }
}
