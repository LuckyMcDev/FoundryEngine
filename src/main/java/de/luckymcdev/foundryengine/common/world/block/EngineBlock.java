package de.luckymcdev.foundryengine.common.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class EngineBlock extends Block {
	private final Map<CallbackType, Object> callbacks = new EnumMap<>(CallbackType.class);
	private @Nullable BiPredicate<Player, BlockState> visibilityCondition;

	public EngineBlock(Properties properties) {
		super(properties);
	}

	private void setCallback(CallbackType type, Object callback) {
		callbacks.put(type, callback);
	}

	public void clearCallback(CallbackType type) {
		callbacks.remove(type);
	}

	public EngineBlock visibilityCondition(@Nullable BiPredicate<Player, BlockState> condition) {
		this.visibilityCondition = condition;
		return this;
	}

	@SuppressWarnings("unchecked")
	private <T> @Nullable T get(CallbackType type) {
		return (T) callbacks.get(type);
	}

	public EngineBlock animateTick(AnimateTickCallback cb) {
		setCallback(CallbackType.ANIMATE_TICK, cb);
		return this;
	}

	public EngineBlock destroy(DestroyCallback cb) {
		setCallback(CallbackType.DESTROY, cb);
		return this;
	}

	public EngineBlock wasExploded(WasExplodedCallback cb) {
		setCallback(CallbackType.WAS_EXPLODED, cb);
		return this;
	}

	public EngineBlock stepOn(StepOnCallback cb) {
		setCallback(CallbackType.STEP_ON, cb);
		return this;
	}

	public EngineBlock setPlacedBy(SetPlacedByCallback cb) {
		setCallback(CallbackType.SET_PLACED_BY, cb);
		return this;
	}

	public EngineBlock fallOn(FallOnCallback cb) {
		setCallback(CallbackType.FALL_ON, cb);
		return this;
	}

	public EngineBlock playerWillDestroy(PlayerWillDestroyCallback cb) {
		setCallback(CallbackType.PLAYER_WILL_DESTROY, cb);
		return this;
	}

	public EngineBlock playerDestroy(PlayerDestroyCallback cb) {
		setCallback(CallbackType.PLAYER_DESTROY, cb);
		return this;
	}

	public EngineBlock handlePrecipitation(HandlePrecipitationCallback cb) {
		setCallback(CallbackType.HANDLE_PRECIPITATION, cb);
		return this;
	}

	public EngineBlock use(UseCallback cb) {
		setCallback(CallbackType.USE, cb);
		return this;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		AnimateTickCallback cb = get(CallbackType.ANIMATE_TICK);
		if (cb != null) {
			cb.run(state, level, pos, random);
		} else {
			super.animateTick(state, level, pos, random);
		}
		if (visibilityCondition != null && random.nextInt(5) == 0) {
			Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 8.0, false);
			if (player != null && player.isCreative() && visibilityCondition.test(player, state)) {
				level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, state),
					pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		DestroyCallback cb = get(CallbackType.DESTROY);
		if (cb != null) {
			cb.run(level, pos, state);
		} else {
			super.destroy(level, pos, state);
		}
	}

	@Override
	public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
		WasExplodedCallback cb = get(CallbackType.WAS_EXPLODED);
		if (cb != null) {
			cb.run(level, pos, explosion);
		} else {
			super.wasExploded(level, pos, explosion);
		}
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState onState, Entity entity) {
		StepOnCallback cb = get(CallbackType.STEP_ON);
		if (cb != null) {
			cb.run(level, pos, onState, entity);
		} else {
			super.stepOn(level, pos, onState, entity);
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
		SetPlacedByCallback cb = get(CallbackType.SET_PLACED_BY);
		if (cb != null) {
			cb.run(level, pos, state, by, itemStack);
		} else {
			super.setPlacedBy(level, pos, state, by, itemStack);
		}
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
		FallOnCallback cb = get(CallbackType.FALL_ON);
		if (cb != null) {
			cb.run(level, state, pos, entity, fallDistance);
		} else {
			super.fallOn(level, state, pos, entity, fallDistance);
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		PlayerWillDestroyCallback cb = get(CallbackType.PLAYER_WILL_DESTROY);
		return cb != null ? cb.run(level, pos, state, player) : super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
		PlayerDestroyCallback cb = get(CallbackType.PLAYER_DESTROY);
		if (cb != null) {
			cb.run(level, player, pos, state, blockEntity, tool);
		} else {
			super.playerDestroy(level, player, pos, state, blockEntity, tool);
		}
	}

	@Override
	public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
		HandlePrecipitationCallback cb = get(CallbackType.HANDLE_PRECIPITATION);
		if (cb != null) {
			cb.run(state, level, pos, precipitation);
		} else {
			super.handlePrecipitation(state, level, pos, precipitation);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		UseCallback cb = get(CallbackType.USE);
		if (cb != null) {
			return cb.run(state, level, pos, player, hitResult);
		}
		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		if (visibilityCondition != null) {
			return RenderShape.INVISIBLE;
		}
		return super.getRenderShape(state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (visibilityCondition == null) {
			return super.getShape(state, level, pos, context);
		}
		if (context instanceof EntityCollisionContext entityCtx
			&& entityCtx.getEntity() instanceof Player player
			&& visibilityCondition.test(player, state)) {
			return Shapes.block();
		}
		return Shapes.empty();
	}

	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		if (visibilityCondition != null) {
			return 1.0F;
		}
		return super.getShadeBrightness(state, level, pos);
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		if (visibilityCondition != null) {
			return true;
		}
		return super.propagatesSkylightDown(state);
	}

	public enum CallbackType {
		ANIMATE_TICK,
		DESTROY,
		WAS_EXPLODED,
		STEP_ON,
		SET_PLACED_BY,
		FALL_ON,
		PLAYER_WILL_DESTROY,
		PLAYER_DESTROY,
		HANDLE_PRECIPITATION,
		USE
	}

	@FunctionalInterface
	public interface AnimateTickCallback {
		void run(BlockState state, Level level, BlockPos pos, RandomSource random);
	}

	@FunctionalInterface
	public interface DestroyCallback {
		void run(LevelAccessor level, BlockPos pos, BlockState state);
	}

	@FunctionalInterface
	public interface WasExplodedCallback {
		void run(ServerLevel level, BlockPos pos, Explosion explosion);
	}

	@FunctionalInterface
	public interface StepOnCallback {
		void run(Level level, BlockPos pos, BlockState onState, Entity entity);
	}

	@FunctionalInterface
	public interface SetPlacedByCallback {
		void run(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack);
	}

	@FunctionalInterface
	public interface FallOnCallback {
		void run(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance);
	}

	@FunctionalInterface
	public interface PlayerWillDestroyCallback {
		BlockState run(Level level, BlockPos pos, BlockState state, Player player);
	}

	@FunctionalInterface
	public interface PlayerDestroyCallback {
		void run(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool);
	}

	@FunctionalInterface
	public interface HandlePrecipitationCallback {
		void run(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation);
	}

	@FunctionalInterface
	public interface UseCallback {
		InteractionResult run(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult);
	}
}