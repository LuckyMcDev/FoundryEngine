package de.luckymcdev.foundryengine.mixin.suggest.nbt;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.NbtSuggestionEngine;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(EntitySelectorParser.class)
public class EntitySelectorParserMixin {
	@Shadow
	@Final
	private StringReader reader;
	@Shadow
	@Nullable
	private EntityType<?> type;

	@Inject(method = "fillSuggestions", at = @At("HEAD"), cancellable = true)
	private void onFillSuggestions(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> names, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
		String remaining = reader.getRemaining();
		if (remaining.startsWith("{") && type != null) {
			String rootType = "entity/" + EntityType.getKey(type);
			cir.setReturnValue(NbtSuggestionEngine.suggest(rootType, builder));
		}
	}
}
