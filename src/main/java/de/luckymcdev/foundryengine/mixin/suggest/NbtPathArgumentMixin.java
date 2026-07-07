package de.luckymcdev.foundryengine.mixin.suggest;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.NbtSuggestionEngine;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.NbtSuggestions;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(NbtPathArgument.class)
public abstract class NbtPathArgumentMixin implements ArgumentType<CompoundTag> {
	@Unique
	private static @Nullable String getRootType(CommandContext<?> ctx) {
		String command = ctx.getNodes().get(0).getNode().getName();
		boolean isExecute = command.equals("execute");

		if (command.equals("data")) {
			return getRootTypeForData(ctx);
		}
		if (isExecute) {
			return getRootTypeForExecute(ctx);
		}

		if (ctx.getChild() != null) {
			return getRootType(ctx.getChild());
		}
		return null;
	}

	@Unique
	private static @Nullable String getRootTypeForData(CommandContext<?> ctx) {
		try {
			String instruction = ctx.getNodes().get(1).getNode().getName();
			String targetType = ctx.getNodes().get(2).getNode().getName();

			String blockArg = "targetPos", entityArg = "target";

			if (instruction.equals("modify")) {
				if (ctx.getNodes().size() > 7) {
					String mod = ctx.getNodes().get(5).getNode().getName();
					if (mod.equals("insert")) {
						targetType = ctx.getNodes().get(8).getNode().getName();
					} else {
						targetType = ctx.getNodes().get(7).getNode().getName();
					}
					blockArg = "sourcePos";
					entityArg = "source";
				}
			}

			return resolveTarget(ctx, targetType, blockArg, entityArg);
		} catch (Exception e) {
			return null;
		}
	}

	@Unique
	private static @Nullable String getRootTypeForExecute(CommandContext<?> ctx) {
		try {
			String sub = ctx.getNodes().get(1).getNode().getName();
			if (!sub.equals("if") && !sub.equals("unless")) {
				return null;
			}

			if (ctx.getNodes().size() < 3) {
				return null;
			}
			String dataNode = ctx.getNodes().get(2).getNode().getName();
			if (!dataNode.equals("data")) {
				return null;
			}

			String targetType = ctx.getNodes().get(3).getNode().getName();
			return resolveTarget(ctx, targetType, "sourcePos", "source");
		} catch (Exception e) {
			return null;
		}
	}

	@Unique
	private static @Nullable String resolveTarget(CommandContext<?> ctx, String type, String blockArg, String entityArg) {
		return switch (type) {
			case "block" -> {
				Coordinates coords = ctx.getArgument(blockArg, Coordinates.class);
				yield NbtSuggestions.getBlockAt(coords);
			}
			case "entity" -> {
				EntitySelector selector = ctx.getArgument(entityArg, EntitySelector.class);
				yield NbtSuggestions.getEntityFrom(selector);
			}
			default -> null;
		};
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
		try {
			String rootType = getRootType(ctx);
			if (rootType == null) {
				return Suggestions.empty();
			}
			return NbtSuggestionEngine.suggest(rootType, builder);
		} catch (Exception e) {
			return Suggestions.empty();
		}
	}
}