package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link WidgetBase} that renders like a vanilla Minecraft slider: a track sprite with a
 * draggable handle, a hover highlight and a centered label. Value is a double in [0, 1].
 */
public class MinecraftSliderWidget extends WidgetBase {
	private static final Identifier SPRITE = Identifier.withDefaultNamespace("widget/slider");
	private static final Identifier SPRITE_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/slider_highlighted");
	private static final Identifier SPRITE_HANDLE = Identifier.withDefaultNamespace("widget/slider_handle");
	private static final Identifier SPRITE_HANDLE_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/slider_handle_highlighted");
	private static final int HANDLE_WIDTH = 8;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int TEXT_COLOR_DISABLED = 0xFFA0A0A0;

	private double value;
	private boolean dragging;
	private Component message;
	private @Nullable OnChange onChange;

	public MinecraftSliderWidget(double initialValue, @NotNull Component message, @Nullable OnChange onChange) {
		this.value = Mth.clamp(initialValue, 0.0, 1.0);
		this.message = message;
		this.onChange = onChange;
	}

	public static MinecraftSliderWidget of(double initialValue, @NotNull Component message, @Nullable OnChange onChange) {
		return new MinecraftSliderWidget(initialValue, message, onChange);
	}

	public double getValue() {
		return this.value;
	}

	public <T extends MinecraftSliderWidget> T setValue(double value) {
		this.setValue(value, false);
		return (T) this;
	}

	private void setValue(double value, boolean notify) {
		double old = this.value;
		this.value = Mth.clamp(value, 0.0, 1.0);
		if (notify && old != this.value && this.onChange != null) {
			this.onChange.onChange(this.value);
		}
	}

	public Component getMessage() {
		return this.message;
	}

	public <T extends MinecraftSliderWidget> T setMessage(@NotNull Component message) {
		this.message = message;
		return (T) this;
	}

	public <T extends MinecraftSliderWidget> T setOnChange(@Nullable OnChange onChange) {
		this.onChange = onChange;
		return (T) this;
	}

	@Override
	void renderContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
		UIArea area = this.getRenderArea(tickDelta);
		boolean hovered = this.contains(mouseX, mouseY);
		Identifier track = this.isEnabled() && (this.isFocused() || hovered) ? SPRITE_HIGHLIGHTED : SPRITE;
		Identifier handle = this.isEnabled() && (hovered || this.dragging) ? SPRITE_HANDLE_HIGHLIGHTED : SPRITE_HANDLE;
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, track, area.x, area.y, area.width, area.height);
		int handleX = area.x + (int) (this.value * (area.width - HANDLE_WIDTH));
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, handle, handleX, area.y, HANDLE_WIDTH, area.height);
		int color = this.isEnabled() ? TEXT_COLOR : TEXT_COLOR_DISABLED;
		guiGraphics.centeredText(Minecraft.getInstance().font, this.message, area.x + area.width / 2, area.y + (area.height - 8) / 2, color);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	private void setValueFromMouse(double mouseX, double mouseY) {
		UIArea area = this.getArea();
		this.setValue((mouseX - area.x) / (area.width - HANDLE_WIDTH), true);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.isEnabled()) {
			return false;
		}
		if (this.contains(mouseX, mouseY)) {
			this.dragging = true;
			this.setValueFromMouse(mouseX, mouseY);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
		if (this.dragging) {
			this.setValueFromMouse(mouseX, mouseY);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.dragging) {
			this.dragging = false;
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			return true;
		}
		return false;
	}

	@FunctionalInterface
	public interface OnChange {
		void onChange(double value);
	}
}
