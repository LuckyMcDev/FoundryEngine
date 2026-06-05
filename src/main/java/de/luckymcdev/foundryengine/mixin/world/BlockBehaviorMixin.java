package de.luckymcdev.foundryengine.mixin.world;

import de.luckymcdev.foundryengine.interfaces.EngineBlockBehavior;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviorMixin implements EngineBlockBehavior {
    @Shadow
    public BlockBehaviour.Properties properties;
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

    @Override
    public void engine$hasCollision(boolean collision) {
        hasCollision = collision;
    }

    @Override
    public void engine$setExplosionResistance(float resistance) {
        explosionResistance = resistance;
    }

    @Override
    public void engine$setIsRandomlyTicking(boolean randomlyTicking) {
        isRandomlyTicking = randomlyTicking;
    }

    @Override
    public void engine$setSoundType(SoundType type) {
        soundType = type;
    }

    @Override
    public void engine$setFriction(float nfriction) {
        friction = nfriction;
    }

    @Override
    public void engine$setSpeedFactor(float nSpeedFactor) {
        speedFactor = nSpeedFactor;
    }

    @Override
    public void engine$setJumpFactor(float nJumpFactor) {
        jumpFactor = nJumpFactor;
    }

    @Override
    public void engine$setLightEmission(int emission) {
        properties.lightLevel(_ -> emission);
    }
}
