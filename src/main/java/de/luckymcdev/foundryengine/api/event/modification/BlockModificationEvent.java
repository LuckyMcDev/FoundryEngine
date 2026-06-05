package de.luckymcdev.foundryengine.api.event.modification;

import de.luckymcdev.foundryengine.interfaces.EngineBlockBehavior;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.Event;

public class BlockModificationEvent extends Event {
    private final Block block;
    private final EngineBlockBehavior behavior;

    public BlockModificationEvent(Block block) {
        this.block = block;
        this.behavior = (EngineBlockBehavior) block;
    }

    public Block getBlock() {
        return block;
    }

    public BlockModificationEvent hasCollision(boolean collision) {
        behavior.engine$hasCollision(collision);
        return this;
    }

    public BlockModificationEvent explosionResistance(float resistance) {
        behavior.engine$setExplosionResistance(resistance);
        return this;
    }

    public BlockModificationEvent randomlyTicking(boolean randomlyTicking) {
        behavior.engine$setIsRandomlyTicking(randomlyTicking);
        return this;
    }

    public BlockModificationEvent soundType(SoundType type) {
        behavior.engine$setSoundType(type);
        return this;
    }

    public BlockModificationEvent friction(float friction) {
        behavior.engine$setFriction(friction);
        return this;
    }

    public BlockModificationEvent speedFactor(float speedFactor) {
        behavior.engine$setSpeedFactor(speedFactor);
        return this;
    }

    public BlockModificationEvent jumpFactor(float jumpFactor) {
        behavior.engine$setJumpFactor(jumpFactor);
        return this;
    }

    public BlockModificationEvent lightEmission(int emission) {
        behavior.engine$setLightEmission(emission);
        return this;
    }
}