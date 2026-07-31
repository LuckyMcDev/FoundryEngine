package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link WidgetBase} that renders like a vanilla Minecraft checkbox: a 17x17 box with the
 * selected check sprite, a hover highlight and a label rendered to the right.
 */
public class MinecraftCheckboxWidget extends WidgetBase {
	private static final Identifier SPRITE = Identifier.withDefaultNamespace("widget/checkbox");
	private static final Identifier SPRITE_SELECTED = Identifier.withDefaultNamespace("widget/checkbox_selected");
	private static final Identifier SPRITE_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/checkbox_highlighted");
	private static final Identifier SPRITE_SELECTED_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted");
	private static final int BOX_SIZE = 17;
	private static final int SPACING = 4;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int TEXT_COLOR_DISABLED = 0xFFA0A0A0;

	private boolean selected;
	private MutableComponent label;
	private @Nullable OnValueChange onValueChange;

	public MinecraftCheckboxWidget(boolean selected, @NotNull Component label, @Nullable OnValueChange onValueChange) {
		this.selected = selected;
		this.label = label.copy();
		this.onValueChange = onValueChange;
	}

	public static MinecraftCheckboxWidget of(boolean selected, @NotNull Component label, @Nullable OnValueChange onValueChange) {
		return new MinecraftCheckboxWidget(selected, label, onValueChange);
	}

	public boolean isSelected() {
		return this.selected;
	}

	public <T extends MinecraftCheckboxWidget> T setSelected(boolean selected) {
		this.selected = selected;
		return (T) this;
	}

	public MutableComponent getLabel() {
		return this.label;
	}

	public <T extends MinecraftCheckboxWidget> T setLabel(@NotNull Component label) {
		this.label = label.copy();
		return (T) this;
	}

	public <T extends MinecraftCheckboxWidget> T setOnValueChange(@Nullable OnValueChange onValueChange) {
		this.onValueChange = onValueChange;
		return (T) this;
	}

	@Override
	void renderContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
		UIArea area = this.getRenderArea(tickDelta);
		int boxY = area.y + (area.height - BOX_SIZE) / 2;
		boolean highlighted = this.isFocused() || this.contains(mouseX, mouseY);
		Identifier sprite;
		if (this.selected) {
			sprite = highlighted ? SPRITE_SELECTED_HIGHLIGHTED : SPRITE_SELECTED;
		} else {
			sprite = highlighted ? SPRITE_HIGHLIGHTED : SPRITE;
		}
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, area.x, boxY, BOX_SIZE, BOX_SIZE);
		int color = this.isEnabled() ? TEXT_COLOR : TEXT_COLOR_DISABLED;
		int textX = area.x + BOX_SIZE + SPACING;
		int textY = area.y + (area.height - 9) / 2;
		guiGraphics.text(Minecraft.getInstance().font, this.label, textX, textY, color);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.isEnabled()) {
			return false;
		}
		if (this.contains(mouseX, mouseY)) {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			this.selected = !this.selected;
			if (this.onValueChange != null) {
				this.onValueChange.onValueChange(this, this.selected);
			}
			return true;
		}
		return false;
	}

	@FunctionalInterface
	public interface OnValueChange {
		void onValueChange(MinecraftCheckboxWidget checkbox, boolean selected);
	}
}
