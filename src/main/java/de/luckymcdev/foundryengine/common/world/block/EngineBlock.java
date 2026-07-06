package de.luckymcdev.foundryengine.common.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class EngineBlock extends Block {
	private final Map<CallbackType, Object> callbacks = new EnumMap<>(CallbackType.class);

	public EngineBlock(Properties properties) {
		super(properties);
	}

	private void setCallback(CallbackType type, Object callback) {
		callbacks.put(type, callback);
	}

	public void clearCallback(CallbackType type) {
		callbacks.remove(type);
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

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		AnimateTickCallback cb = get(CallbackType.ANIMATE_TICK);
		if (cb != null) {
			cb.run(state, level, pos, random);
		} else {
			super.animateTick(state, level, pos, random);
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

	public enum CallbackType {
		ANIMATE_TICK,
		DESTROY,
		WAS_EXPLODED,
		STEP_ON,
		SET_PLACED_BY,
		FALL_ON,
		PLAYER_WILL_DESTROY,
		PLAYER_DESTROY,
		HANDLE_PRECIPITATION
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
}