package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.game.stage.GameStageHandler;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;

import java.util.Collection;
import java.util.Set;

/**
 * Command to modify stages. See {@link GameStageHandler}
 */
public class StageCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        return Commands.literal("stage")
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.literal("add").requires(this::isAdmin)
                                .then(Commands.argument("stage", StringArgumentType.word())
                                        .executes(ctx -> modifyStage(ctx, TriState.TRUE))))
                        .then(Commands.literal("remove").requires(this::isAdmin)
                                .then(Commands.argument("stage", StringArgumentType.word())
                                        .executes(ctx -> modifyStage(ctx, TriState.FALSE))))
                        .then(Commands.literal("clear").requires(this::isAdmin)
                                .executes(ctx -> modifyStage(ctx, TriState.DEFAULT)))
                        .then(Commands.literal("list")
                                .executes(this::listStages)));
    }

    private int modifyStage(CommandContext<CommandSourceStack> ctx, TriState type) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
        String stage = type.isDefault() ? "" : StringArgumentType.getString(ctx, "stage");
        int count = 0;

        for (ServerPlayer player : players) {
            if (type.isTrue()) {
                Common.getGameStageHandler().addStage(player, stage);
            } else if (type.isFalse()) {
                Common.getGameStageHandler().removeStage(player, stage);
            } else {
                Common.getGameStageHandler().clearStages(player);
            }
            count++;
        }

        String action = type.isTrue() ? "Added" : type.isFalse() ? "Removed" : "Cleared all";
        String stageSuffix = type.isDefault() ? "" : String.format(" '%s'", stage);
        int finalCount = count;
        ctx.getSource().sendSuccess(() -> Component.literal(
                String.format("%s%s for %d player(s)", action, stageSuffix, finalCount)
        ), true);

        return count;
    }

    private int listStages(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(ctx, "targets");

        for (ServerPlayer player : players) {
            Set<String> stages = Common.getGameStageHandler().getStages(player);
            String stageList = stages.isEmpty() ? "None" : String.join(", ", stages);

            ctx.getSource().sendSuccess(() -> Component.literal(
                    String.format("Player %s has stages: [%s]", player.getGameProfile().name(), stageList)
            ), false);
        }

        return players.size();
    }
}