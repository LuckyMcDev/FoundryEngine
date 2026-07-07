package de.luckymcdev.foundryengine.mixin.suggest;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.NbtSuggestionEngine;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(BlockStateParser.class)
public class BlockStateParserMixin {
	@Shadow
	@Final
	private StringReader reader;
	@Shadow
	@Nullable
	private BlockState state;
	@Shadow
	@Nullable
	private CompoundTag nbt;
	@Shadow
	private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions;

	@Inject(method = "readNbt", at = @At("HEAD"), cancellable = true)
	private void onReadNbt(CallbackInfo ci) throws CommandSyntaxException {
		ci.cancel();
		int cursorPos = reader.getCursor();

		try {
			nbt = TagParser.parseCompoundAsArgument(reader);
		} catch (CommandSyntaxException e) {
			reader.setCursor(cursorPos);
			suggestions = this::suggestNbt;
			throw e;
		}
	}

	@Unique
	private CompletableFuture<Suggestions> suggestNbt(SuggestionsBuilder builder) {
		if (state == null) {
			return Suggestions.empty();
		}
		Identifier id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
		return NbtSuggestionEngine.suggest("block/" + id, builder);
	}
}