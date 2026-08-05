package de.luckymcdev.foundryengine.mixin.world;

import de.luckymcdev.foundryengine.interfaces.world.EngineBlockStateBehavior;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Implements {@link EngineBlockStateBehavior} on BlockStateBase via accessor mixins.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin implements EngineBlockStateBehavior {
	@Accessor("lightEmission")
	@Mutable
	@Override
	public abstract void engine$setLightEmission(int emission);

	@Accessor("destroySpeed")
	@Mutable
	@Override
	public abstract void engine$setDestroySpeed(float speed);

	@Accessor("requiresCorrectToolForDrops")
	@Mutable
	@Override
	public abstract void engine$setRequiresTool(boolean requiresTool);
}
