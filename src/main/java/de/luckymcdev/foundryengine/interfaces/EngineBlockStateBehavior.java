package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.world.level.block.state.BlockBehaviour;

public interface EngineBlockStateBehavior extends EngineInterface<BlockBehaviour.BlockStateBase> {
    void engine$setLightEmission(int emission);

    void engine$setDestroySpeed(float speed);

    void engine$setRequiresTool(boolean requiresTool);
}
