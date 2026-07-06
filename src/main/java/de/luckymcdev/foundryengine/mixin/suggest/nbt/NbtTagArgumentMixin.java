package de.luckymcdev.foundryengine.mixin.suggest.nbt;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.NbtSuggestionEngine;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.NbtSuggestions;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(NbtTagArgument.class)
public abstract class NbtTagArgumentMixin implements ArgumentType<Tag> {
	@Unique
	private static @Nullable String getRootType(CommandContext<?> ctx) {
		String command = ctx.getNodes().get(0).getNode().getName();
		return switch (command) {
			case "data" -> getRootTypeForData(ctx);
			default -> ctx.getChild() != null ? getRootType(ctx.getChild()) : null;
		};
	}

	@Unique
	private static @Nullable String getRootTypeForData(CommandContext<?> ctx) {
		try {
			String instruction = ctx.getNodes().get(1).getNode().getName();
			if (!instruction.equals("modify")) {
				return null;
			}

			String targetType = ctx.getNodes().get(2).getNode().getName();
			return switch (targetType) {
				case "block" -> {
					Coordinates coords = ctx.getArgument("targetPos", Coordinates.class);
					yield NbtSuggestions.getBlockAt(coords);
				}
				case "entity" -> {
					EntitySelector selector = ctx.getArgument("target", EntitySelector.class);
					yield NbtSuggestions.getEntityFrom(selector);
				}
				default -> null;
			};
		} catch (Exception e) {
			return null;
		}
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