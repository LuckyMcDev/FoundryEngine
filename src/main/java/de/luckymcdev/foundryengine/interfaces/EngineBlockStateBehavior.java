package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Configures block state behavior properties such as light emission, destroy speed, and tool requirements.
 */
public interface EngineBlockStateBehavior extends EngineInterface<BlockBehaviour.BlockStateBase> {
    /**
     * Sets the light emission level for this block state.
     */
    void engine$setLightEmission(int emission);

    /**
     * Sets the destroy speed of this block state.
     */
    void engine$setDestroySpeed(float speed);

    /**
     * Sets whether this block state requires a tool to drop.
     */
    void engine$setRequiresTool(boolean requiresTool);
}
