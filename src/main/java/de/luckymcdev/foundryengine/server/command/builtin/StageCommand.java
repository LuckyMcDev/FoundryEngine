package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Set;

public class StageCommand implements EngineCommand {
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_STAGES = (ctx, builder) ->
		SharedSuggestionProvider.suggest(
			Common.getGameStageHandler().getStageRegistry().getStages().stream()
				.map(Identifier::toString),
			builder
		);

	private static final SuggestionProvider<CommandSourceStack> SUGGEST_TABLES = (ctx, builder) ->
		SharedSuggestionProvider.suggest(
			Common.getStageTableManager().getTableNames().stream()
				.map(Identifier::toString),
			builder
		);

	@Override
	public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
		return Commands.literal("stage")
			.then(Commands.argument("targets", EntityArgument.players())
				.then(Commands.literal("add").requires(this::isAdmin)
					.then(Commands.argument("stage", IdentifierArgument.id())
						.suggests(SUGGEST_STAGES)
						.executes(ctx -> modifyStage(ctx, true))))
				.then(Commands.literal("remove").requires(this::isAdmin)
					.then(Commands.argument("stage", IdentifierArgument.id())
						.suggests(SUGGEST_STAGES)
						.executes(ctx -> modifyStage(ctx, false))))
				.then(Commands.literal("clear").requires(this::isAdmin)
					.executes(ctx -> modifyStage(ctx, null)))
				.then(Commands.literal("list")
					.executes(this::listStages))
				.then(Commands.literal("table").requires(this::isAdmin)
					.then(Commands.literal("award")
						.then(Commands.argument("table", IdentifierArgument.id())
							.suggests(SUGGEST_TABLES)
							.executes(this::awardTable)))
					.then(Commands.literal("silentaward")
						.then(Commands.argument("table", IdentifierArgument.id())
							.suggests(SUGGEST_TABLES)
							.executes(ctx -> awardTable(ctx, true))))
					.then(Commands.literal("list")
						.executes(this::listTables))));
	}

	private int modifyStage(CommandContext<CommandSourceStack> ctx, Boolean add) throws CommandSyntaxException {
		Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
		int count = 0;

		for (ServerPlayer player : players) {
			if (add == null) {
				Common.getGameStageHandler().clearStages(player);
			} else {
				Identifier stage = IdentifierArgument.getId(ctx, "stage");
				if (add) {
					Common.getGameStageHandler().addStage(player, stage);
				} else {
					Common.getGameStageHandler().removeStage(player, stage);
				}
			}
			count++;
		}

		String action = add == null ? "Cleared all stages" : add ? "Added" : "Removed";
		String stageSuffix = add == null ? "" : " " + IdentifierArgument.getId(ctx, "stage");
		int finalCount = count;
		ctx.getSource().sendSuccess(() -> Component.literal(
			String.format("%s%s for %d player(s)", action, stageSuffix, finalCount)
		), true);

		return count;
	}

	private int listStages(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(ctx, "targets");

		for (ServerPlayer player : players) {
			Set<Identifier> stages = Common.getGameStageHandler().getStages(player);
			String stageList = stages.isEmpty() ? "None" : String.join(", ", stages.stream().map(Identifier::toString).toList());

			ctx.getSource().sendSuccess(() -> Component.literal(
				String.format("Player %s has stages: [%s]", player.getGameProfile().name(), stageList)
			), false);
		}

		return players.size();
	}

	private int awardTable(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		return awardTable(ctx, false);
	}

	private int awardTable(CommandContext<CommandSourceStack> ctx, boolean silent) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(ctx, "targets");
		var tableName = IdentifierArgument.getId(ctx, "table");
		var table = Common.getStageTableManager().getTable(tableName);
		int count = 0;

		if (table == null) {
			ctx.getSource().sendFailure(Component.literal("No stage table found: " + tableName));
			return 0;
		}

		for (var player : players) {
			if (!table.canPlayerUse(player)) {
				if (!silent) {
					player.sendSystemMessage(Component.translatable("foundryengine.stagetable.ineligible", tableName.toString()));
				}
				continue;
			}

			boolean awarded = false;
			while (!awarded) {
				var random = player.level().getRandom();
				var entry = table.getRandomEntry(random);
				if (entry == null) {
					break;
				}
				if (entry.canPlayerObtain(player)) {
					awarded = true;
					Common.getGameStageHandler().addStage(player, entry.getStage());
					if (!silent) {
						player.sendSystemMessage(Component.translatable("commands.gamestage.add.target", entry.getStage().toString()));
						var source = ctx.getSource();
						if (source.getEntity() != player) {
							source.sendSuccess(() -> Component.translatable(
								"commands.gamestage.add.sender", entry.getStage().toString(), player.getGameProfile().name()
							), true);
						}
					}
					count++;
				}
			}
		}

		return count;
	}

	private int listTables(CommandContext<CommandSourceStack> ctx) {
		var manager = Common.getStageTableManager();
		if (manager.size() == 0) {
			ctx.getSource().sendSuccess(() -> Component.literal("No stage tables registered."), false);
			return 0;
		}

		for (var table : manager.getTables()) {
			var name = table.getName();
			int totalWeight = table.getTotalWeight();
			int entryCount = table.getEntries().size();
			ctx.getSource().sendSuccess(() -> Component.literal(
				String.format("%s — %d entries, total weight %d", name, entryCount, totalWeight)
			), false);
		}

		return manager.size();
	}
}
