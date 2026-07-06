package de.luckymcdev.foundryengine.mixin.suggest.nbt;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.NbtSuggestionEngine;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.NbtSuggestions;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(CompoundTagArgument.class)
public abstract class CompoundTagArgumentMixin implements ArgumentType<CompoundTag> {
	@Unique
	private static @Nullable String getRootType(CommandContext<?> ctx) {
		String command = ctx.getNodes().get(0).getNode().getName();
		if (command.equals("minecraft:")) {
			command = command.substring(10);
		}

		return switch (command) {
			case "summon" -> {
				EntityType<?> type = ((Holder.Reference<EntityType<?>>) ctx.getArgument("entity", Holder.Reference.class)).value();
				yield "entity/" + EntityType.getKey(type);
			}
			case "data" -> getRootTypeForData(ctx);
			default -> ctx.getChild() != null ? getRootType(ctx.getChild()) : null;
		};
	}

	@Unique
	private static @Nullable String getRootTypeForData(CommandContext<?> ctx) {
		try {
			String instruction = ctx.getNodes().get(1).getNode().getName();
			if (!instruction.equals("merge")) {
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