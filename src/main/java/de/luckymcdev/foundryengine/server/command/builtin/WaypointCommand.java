package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.waypoint.Waypoint;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class WaypointCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        return Commands.literal("waypoint")
                .requires(this::isGamemaster)
                .then(Commands.literal("add")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> addWaypoint(ctx, "I", 0xFF40C0C0))
                                        .then(Commands.argument("icon", StringArgumentType.word())
                                                .executes(ctx -> addWaypoint(ctx, StringArgumentType.getString(ctx, "icon"), 0xFF40C0C0))
                                                .then(Commands.argument("color", IntegerArgumentType.integer())
                                                        .executes(ctx -> addWaypoint(ctx,
                                                                StringArgumentType.getString(ctx, "icon"),
                                                                IntegerArgumentType.getInteger(ctx, "color"))))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(this::removeWaypoint)))
                .then(Commands.literal("clear")
                        .executes(this::clearWaypoints))
                .then(Commands.literal("list")
                        .executes(this::listWaypoints));
    }

    private int addWaypoint(CommandContext<CommandSourceStack> ctx, String icon, int color) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
        String name = StringArgumentType.getString(ctx, "name");

        Common.getWaypointManager().addWaypoint(level, new Waypoint(name, icon, pos.getX(), pos.getY(), pos.getZ(), color));
        Common.getSavedDataManager().syncToDimension(level);
        sendSuccess(ctx, "Added waypoint [" + name + "] at " + pos.toShortString(), true);
        return 1;
    }

    private int removeWaypoint(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");

        if (Common.getWaypointManager().removeWaypoint(level, pos.getX(), pos.getY(), pos.getZ())) {
            Common.getSavedDataManager().syncToDimension(level);
            sendSuccess(ctx, "Removed waypoint at " + pos.toShortString(), true);
            return 1;
        }
        sendFailure(ctx, "No waypoint found at " + pos.toShortString());
        return 0;
    }

    private int clearWaypoints(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        Common.getWaypointManager().clearWaypoints(level);
        Common.getSavedDataManager().syncToDimension(level);
        sendSuccess(ctx, "All waypoints cleared.", true);
        return 1;
    }

    private int listWaypoints(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        List<Waypoint> waypoints = Common.getWaypointManager().getWaypoints(level.dimension());
        if (waypoints.isEmpty()) {
            sendInfo(ctx, "No waypoints in this dimension.");
            return 1;
        }

        sendInfo(ctx, "Waypoints (" + waypoints.size() + "):");
        for (Waypoint w : waypoints) {
            sendInfo(ctx, "  [" + w.name() + "] at (" + w.x() + ", " + w.y() + ", " + w.z() + ")");
        }
        return 1;
    }
}
