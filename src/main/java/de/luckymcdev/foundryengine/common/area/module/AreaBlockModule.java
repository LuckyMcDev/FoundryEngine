package de.luckymcdev.foundryengine.common.area.module;

import de.luckymcdev.foundryengine.common.area.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jspecify.annotations.Nullable;

public interface AreaBlockModule extends AreaModule {
	default void onBlockBreak(BreakBlockEvent event, ServerLevel level, Area area, BlockPos pos, BlockState state, @Nullable ServerPlayer player) {
	}

	default void onBlockPlace(BlockEvent.EntityPlaceEvent event, ServerLevel level, Area area, BlockPos pos, BlockState state, @Nullable ServerPlayer player) {
	}
}
