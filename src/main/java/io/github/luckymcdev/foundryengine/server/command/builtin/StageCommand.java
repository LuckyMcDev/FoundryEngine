package io.github.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.luckymcdev.foundryengine.common.game.stage.GameStageHandler;
import io.github.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public class StageCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("stage")
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.literal("add").requires(this::isAdmin)
                                .then(Commands.argument("stage", StringArgumentType.word())
                                        .executes(ctx -> modifyStage(ctx, true))))
                        .then(Commands.literal("remove").requires(this::isAdmin)
                                .then(Commands.argument("stage", StringArgumentType.word())
                                        .executes(ctx -> modifyStage(ctx, false))))
                        .then(Commands.literal("list")
                                .executes(this::listStages)));
    }

    private int modifyStage(CommandContext<CommandSourceStack> ctx, boolean add) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(ctx, "targets");
        String stage = StringArgumentType.getString(ctx, "stage");
        int count = 0;

        for (ServerPlayer player : players) {
            if (add ? GameStageHandler.addStage(player, stage) : GameStageHandler.removeStage(player, stage)) {
                count++;
            }
        }

        String action = add ? "Added" : "Removed";
        sendSuccess(ctx, String.format("%s stage '%s' for %d player(s)", action, stage, count), true);

        return count;
    }

    private int listStages(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(ctx, "targets");

        for (ServerPlayer player : players) {
            Set<String> stages = GameStageHandler.getStages(player);
            String stageList = stages.isEmpty() ? "None" : String.join(", ", stages);

            ctx.getSource().sendSuccess(() -> Component.literal(
                    String.format("Player %s has stages: [%s]", player.getGameProfile().name(), stageList)
            ), false);
        }

        return players.size();
    }
}