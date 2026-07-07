package de.luckymcdev.foundryengine.mixin.suggest;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.JsonComponentSuggestions;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

@Mixin(StyleArgument.class)
public abstract class StyleArgumentMixin implements ArgumentType<Style> {
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
		try {
			return JsonComponentSuggestions.suggestStyle(builder);
		} catch (Exception e) {
			return Suggestions.empty();
		}
	}
}