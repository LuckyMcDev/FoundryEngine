package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.dialogue.DialogueDisplayMode;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.stream.Collectors;

/**
 * {@code /engine dialogue} — controls the dialogue system.
 * <p>
 * Gamemaster subcommands: start, list, end {@code <player>}.
 * Player subcommands (no permission check): next, end, select {@code <index>}.
 */
public class DialogueCommand implements EngineCommand {

    private static final SuggestionProvider<CommandSourceStack> TREE_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Common.getDialogueManager().getTrees().stream()
                            .map(t -> t.getId().toString())
                            .collect(Collectors.toSet()),
                    builder
            );

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        return Commands.literal("dialogue")
                .then(Commands.literal("start")
                        .requires(this::isGamemaster)
                        .then(Commands.argument("tree", IdentifierArgument.id())
                                .suggests(TREE_SUGGESTIONS)
                                .executes(ctx -> startForSelf(ctx, DialogueDisplayMode.SCREEN))
                                .then(Commands.literal("screen")
                                        .executes(ctx -> startForSelf(ctx, DialogueDisplayMode.SCREEN))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> startForPlayer(ctx, DialogueDisplayMode.SCREEN))))
                                .then(Commands.literal("chat")
                                        .executes(ctx -> startForSelf(ctx, DialogueDisplayMode.CHAT))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> startForPlayer(ctx, DialogueDisplayMode.CHAT))))))
                .then(Commands.literal("list")
                        .requires(this::isGamemaster)
                        .executes(this::listTrees))
                .then(Commands.literal("next")
                        .executes(this::advance))
                .then(Commands.literal("select")
                        .then(Commands.argument("index", IntegerArgumentType.integer(1))
                                .executes(this::selectOption)))
                .then(Commands.literal("end")
                        .executes(ctx -> endSelf(ctx))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(this::isGamemaster)
                                .executes(ctx -> endPlayer(ctx))));
    }

    private int startForSelf(CommandContext<CommandSourceStack> ctx, DialogueDisplayMode mode) throws CommandSyntaxException {
        var player = getPlayer(ctx);
        var treeId = IdentifierArgument.getId(ctx, "tree");
        startDialogue(ctx, player, treeId, mode);
        return 1;
    }

    private int startForPlayer(CommandContext<CommandSourceStack> ctx, DialogueDisplayMode mode) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(ctx, "player");
        var treeId = IdentifierArgument.getId(ctx, "tree");
        startDialogue(ctx, player, treeId, mode);
        return 1;
    }

    private void startDialogue(CommandContext<CommandSourceStack> ctx, ServerPlayer target, Identifier treeId, DialogueDisplayMode mode) {
        var mgr = Common.getDialogueManager();
        var tree = mgr.getTree(treeId);
        if (tree == null) {
            sendFailure(ctx, "Dialogue tree not found: " + treeId);
            return;
        }
        mgr.startDialogue(target, treeId, mode);
        sendSuccess(ctx, "Started dialogue '" + treeId + "' (" + mode.name().toLowerCase() + ") for " + target.getName().getString(), true);
    }

    private int advance(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = getPlayer(ctx);
        var mgr = Common.getDialogueManager();
        if (!mgr.hasActiveSession(player)) {
            sendFailure(ctx, "No active dialogue.");
            return 0;
        }
        mgr.advanceNext(player);
        return 1;
    }

    private int selectOption(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = getPlayer(ctx);
        var index = IntegerArgumentType.getInteger(ctx, "index") - 1;
        var mgr = Common.getDialogueManager();
        var session = mgr.getSession(player);
        if (session == null || session.isEnded()) {
            sendFailure(ctx, "No active dialogue.");
            return 0;
        }
        var tree = mgr.getTree(session.getTreeId());
        if (tree == null) return 0;
        var node = tree.getNode(session.getCurrentNodeId());
        if (node == null) return 0;
        var options = node.getOptions();
        if (index < 0 || index >= options.size()) {
            sendFailure(ctx, "Invalid option index. Choose 1-" + options.size() + ".");
            return 0;
        }
        mgr.selectOption(player, options.get(index).getId());
        return 1;
    }

    private int endSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = getPlayer(ctx);
        Common.getDialogueManager().endDialogue(player);
        sendSuccess(ctx, "Dialogue ended.", true);
        return 1;
    }

    private int endPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = EntityArgument.getPlayer(ctx, "player");
        Common.getDialogueManager().endDialogue(player);
        sendSuccess(ctx, "Ended dialogue for " + player.getName().getString(), true);
        return 1;
    }

    private int listTrees(CommandContext<CommandSourceStack> ctx) {
        var trees = Common.getDialogueManager().getTrees();
        if (trees.isEmpty()) {
            sendInfo(ctx, "No dialogue trees registered.");
        } else {
            sendInfo(ctx, "Registered dialogue trees (" + trees.size() + "):");
            for (var t : trees) {
                int nodes = t.getNodes().size();
                sendInfo(ctx, "  " + t.getId() + " (" + nodes + " nodes, root: " + t.getRootNodeId() + ")");
            }
        }
        return 1;
    }
}
