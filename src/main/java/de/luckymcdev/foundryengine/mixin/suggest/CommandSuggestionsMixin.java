package de.luckymcdev.foundryengine.mixin.suggest;

import de.luckymcdev.foundryengine.client.command.suggest.nbt.SuggestionData;
import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {
	@Inject(method = "updateCommandInfo", at = @At("HEAD"))
	private void onUpdateCommandInfo(CallbackInfo ci) {
		SuggestionData.clear();
	}
}
