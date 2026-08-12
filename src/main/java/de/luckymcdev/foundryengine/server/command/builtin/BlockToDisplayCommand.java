package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.world.entity.EntitySpawner;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
//? if 26.1 {
import net.minecraft.world.entity.EntityType;
 //?} elif 26.2 {
/*import net.minecraft.world.entity.EntityTypes;
*///?}
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BlockToDisplayCommand implements EngineCommand {
	private static final int MAX_REGION_BLOCKS = 4096;
	private static final String NAME = "block_to_display";
	private static final String ARG_POS = "pos1";
	private static final String ARG_POS_2 = "pos2";
	private static final String ARG_REPLACE = "replace_with_air";
	private static final String ARG_TAG = "custom_tag";

	@Override
	public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
		return Commands.literal(NAME)
			// 0 Args (Looking at block)
			.executes(ctx -> executeSingle(ctx, null, false, null))
			.then(Commands.argument(ARG_REPLACE, BoolArgumentType.bool())
				.executes(ctx -> executeSingle(ctx, null, BoolArgumentType.getBool(ctx, ARG_REPLACE), null))
				.then(Commands.argument(ARG_TAG, StringArgumentType.string())
					.executes(ctx -> executeSingle(ctx, null, BoolArgumentType.getBool(ctx, ARG_REPLACE), StringArgumentType.getString(ctx, ARG_TAG)))))

			// 1 Arg (Specific block)
			.then(Commands.argument(ARG_POS, BlockPosArgument.blockPos())
				.executes(ctx -> executeSingle(ctx, BlockPosArgument.getBlockPos(ctx, ARG_POS), false, null))
				.then(Commands.argument(ARG_REPLACE, BoolArgumentType.bool())
					.executes(ctx -> executeSingle(ctx, BlockPosArgument.getBlockPos(ctx, ARG_POS), BoolArgumentType.getBool(ctx, ARG_REPLACE), null))
					.then(Commands.argument(ARG_TAG, StringArgumentType.string())
						.executes(ctx -> executeSingle(ctx, BlockPosArgument.getBlockPos(ctx, ARG_POS), BoolArgumentType.getBool(ctx, ARG_REPLACE), StringArgumentType.getString(ctx, ARG_TAG)))))

				// 2 Args (Region)
				.then(Commands.argument(ARG_POS_2, BlockPosArgument.blockPos())
					.executes(ctx -> executeRegion(ctx, BlockPosArgument.getBlockPos(ctx, ARG_POS), BlockPosArgument.getBlockPos(ctx, ARG_POS_2), false, null))
					.then(Commands.argument(ARG_REPLACE, BoolArgumentType.bool())
						.executes(ctx -> executeRegion(ctx, BlockPosArgument.getBlockPos(ctx, ARG_POS), BlockPosArgument.getBlockPos(ctx, ARG_POS_2), BoolArgumentType.getBool(ctx, ARG_REPLACE), null))
						.then(Commands.argument(ARG_TAG, StringArgumentType.string())
							.executes(ctx -> executeRegion(ctx, BlockPosArgument.getBlockPos(ctx, ARG_POS), BlockPosArgument.getBlockPos(ctx, ARG_POS_2), BoolArgumentType.getBool(ctx, ARG_REPLACE), StringArgumentType.getString(ctx, ARG_TAG)))))));
	}

	/**
	 * Spawn a single block display.
	 * If pos is null, use the block the player is looking at (or standing on).
	 */
	private int executeSingle(CommandContext<CommandSourceStack> context, @Nullable BlockPos pos, boolean replace, @Nullable String tag) {
		CommandSourceStack source = context.getSource();
		var level = source.getLevel();

		if (pos == null) {
			if (source.getEntity() instanceof Player player) {
				double reach = player.blockInteractionRange();
				HitResult hit = player.pick(reach, 1.0F, false);
				if (hit.getType() == HitResult.Type.BLOCK) {
					pos = ((BlockHitResult) hit).getBlockPos();
				} else {
					pos = BlockPos.containing(source.getPosition());
				}
			} else {
				pos = BlockPos.containing(source.getPosition());
			}
		}

		spawnBlockDisplay(level, pos, replace, tag);
		return 1;
	}

	/**
	 * Spawn block displays for every block in the axis‑aligned bounding box
	 * defined by pos1 and pos2.
	 */
	private int executeRegion(CommandContext<CommandSourceStack> context, BlockPos pos1, BlockPos pos2, boolean replace, @Nullable String tag) {
		var level = context.getSource().getLevel();

		int minX = Math.min(pos1.getX(), pos2.getX());
		int maxX = Math.max(pos1.getX(), pos2.getX());
		int minY = Math.min(pos1.getY(), pos2.getY());
		int maxY = Math.max(pos1.getY(), pos2.getY());
		int minZ = Math.min(pos1.getZ(), pos2.getZ());
		int maxZ = Math.max(pos1.getZ(), pos2.getZ());

		int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
		if (volume > MAX_REGION_BLOCKS) {
			context.getSource().sendFailure(
				Component.literal(
					"Region too large! Max " + MAX_REGION_BLOCKS + " blocks."
				)
			);
			return 0;
		}

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					mutablePos.set(x, y, z);
					spawnBlockDisplay(level, mutablePos, replace, tag);
				}
			}
		}

		context.getSource().sendSuccess(
			() -> Component.literal("Spawned " + volume + " block displays."),
			false
		);
		return 1;
	}

	private void spawnBlockDisplay(ServerLevel level, BlockPos pos, boolean replace, @Nullable String tag) {
		BlockState state = level.getBlockState(pos);
		Vec3 center = new Vec3(pos.getX(), pos.getY(), pos.getZ());

		Display.BlockDisplay display = EntitySpawner.spawnServer(
			level,
			//? if 26.1 {
			EntityType.BLOCK_DISPLAY,
			 //?} elif 26.2 {
			/*EntityTypes.BLOCK_DISPLAY,
			*///?}
			center,
			entity -> {
				entity.setBlockState(state);
				entity.addTag(Common.BLOCK_DISPLAY_TAG);
				if (tag != null && !tag.isBlank()) {
					entity.addTag(tag);
				}
			}
		);

		if (replace) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
	}
}