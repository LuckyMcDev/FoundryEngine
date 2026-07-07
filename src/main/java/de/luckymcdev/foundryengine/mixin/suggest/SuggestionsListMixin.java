package de.luckymcdev.foundryengine.mixin.suggest;

import com.mojang.brigadier.suggestion.Suggestion;
import de.luckymcdev.foundryengine.client.command.suggest.nbt.SuggestionData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public abstract class SuggestionsListMixin {
	@Shadow
	@Final
	private Rect2i rect;
	@Shadow
	@Final
	private List<Suggestion> suggestionList;
	@Shadow
	private int offset;
	@Unique
	private Font fontToUse;
	@Unique
	private boolean addTypeNames;
	@Unique
	private int renderLoopI;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onConstruct(CommandSuggestions commandSuggestions, int x, int y, int width,
	                         List<Suggestion> suggestions, boolean narrated, CallbackInfo ci) {
		if (!SuggestionData.hasCustomSuggestions) {
			return;
		}

		fontToUse = ((CommandSuggestionsAccessor) commandSuggestions).getFont();
		initSubtext();
		sortByPriority();
	}

	@Unique
	private void initSubtext() {
		int newWidth = rect.getWidth();
		for (Suggestion suggestion : suggestionList) {
			String text = suggestion.getText();
			String subtext = SuggestionData.getSubtext(text);
			if (subtext != null) {
				addTypeNames = true;
				newWidth = Math.max(newWidth, fontToUse.width(text) + fontToUse.width(subtext) + 3);
			}
		}
		if (addTypeNames && newWidth > rect.getWidth()) {
			rect.setWidth(newWidth);
		}
	}

	@Unique
	private void sortByPriority() {
		suggestionList.sort(Comparator.comparingInt(
			s -> -SuggestionData.getPriority(s.getText())
		));
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void onRenderStart(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
		renderLoopI = 0;
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void drawSubtexts(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
		if (!addTypeNames) {
			return;
		}

		int limit = Math.min(suggestionList.size(), Math.max(1, rect.getHeight() / 12));
		for (int i = 0; i < limit; i++) {
			int idx = i + offset;
			if (idx >= suggestionList.size()) {
				break;
			}
			String subtext = SuggestionData.getSubtext(suggestionList.get(idx).getText());
			if (subtext == null) {
				continue;
			}

			guiGraphics.text(fontToUse, subtext,
				rect.getX() + rect.getWidth() - fontToUse.width(subtext) - 1,
				rect.getY() + 2 + 12 * i, 0xFF555555);
		}
	}

	@ModifyArg(method = "extractRenderState",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"),
		index = 4)
	private int modifyTextColor(int color) {
		if (!SuggestionData.hasCustomSuggestions || suggestionList.isEmpty()) {
			return color;
		}

		int idx = Math.clamp(renderLoopI + offset, 0, suggestionList.size() - 1);
		int priority = SuggestionData.getPriority(suggestionList.get(idx).getText());
		renderLoopI++;

		if (priority >= 0) {
			return color;
		}
		return switch (color) {
			case 0xFFAAAAAA -> 0xFF555555;
			case 0xFFFFFF00 -> 0xFF888800;
			default -> color;
		};
	}
}
