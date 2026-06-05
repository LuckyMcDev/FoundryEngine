package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface EngineBlockBehavior extends EngineInterface<BlockBehaviour> {
    void engine$hasCollision(boolean collision);
    void engine$setExplosionResistance(float resistance);
    void engine$setIsRandomlyTicking(boolean randomlyTicking);
    void engine$setSoundType(SoundType type);
    void engine$setFriction(float friction);
    void engine$setSpeedFactor(float speedFactor);
    void engine$setJumpFactor(float jumpFactor);

    void engine$setLightEmission(int emission);
}
