package de.luckymcdev.foundryengine.common.world.block;

import de.luckymcdev.foundryengine.common.world.block.entity.EngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class EngineEntityBlock extends EngineBlock implements EntityBlock {
	private final Supplier<BlockEntityType<?>> blockEntityType;
	private final boolean hasServerTick;

	public EngineEntityBlock(Properties properties, Supplier<BlockEntityType<?>> blockEntityType, boolean hasServerTick) {
		super(properties);
		this.blockEntityType = blockEntityType;
		this.hasServerTick = hasServerTick;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return blockEntityType.get().create(pos, state);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (type != blockEntityType.get()) {
			return null;
		}
		if (!hasServerTick || level.isClientSide()) {
			return null;
		}
		return (lvl, pos, st, be) -> {
			if (be instanceof EngineBlockEntity ebe) {
				ebe.tick(lvl, pos, st);
			}
		};
	}
}
