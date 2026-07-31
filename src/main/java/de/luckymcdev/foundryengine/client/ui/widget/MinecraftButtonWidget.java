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

/**
 * A {@link ButtonWidget} that renders and behaves like a vanilla Minecraft button:
 * nine-slice sprite (enabled / highlighted / disabled), centered label with drop shadow,
 * a click sound and the pressed label offset while held.
 */
public class MinecraftButtonWidget extends ButtonWidget {
	private static final Identifier SPRITE = Identifier.withDefaultNamespace("widget/button");
	private static final Identifier SPRITE_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");
	private static final Identifier SPRITE_DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int TEXT_COLOR_DISABLED = 0xFFA0A0A0;

	private MutableComponent text;
	private boolean pressed;

	public MinecraftButtonWidget(@NotNull Component text, ClickEvent clickEvent) {
		super(clickEvent);
		this.text = text.copy();
	}

	public static MinecraftButtonWidget of(@NotNull Component text, ClickEvent clickEvent) {
		return new MinecraftButtonWidget(text, clickEvent);
	}

	public MutableComponent getText() {
		return this.text;
	}

	public <T extends MinecraftButtonWidget> T setText(@NotNull Component text) {
		this.text = text.copy();
		return (T) this;
	}

	public <T extends MinecraftButtonWidget> T setText(String text) {
		this.text = Component.literal(text);
		return (T) this;
	}

	@Override
	void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
		UIArea area = this.getRenderArea(tickDelta);
		Identifier sprite;
		if (!this.isEnabled()) {
			sprite = SPRITE_DISABLED;
		} else if (this.contains(mouseX, mouseY) || this.isFocused()) {
			sprite = SPRITE_HIGHLIGHTED;
		} else {
			sprite = SPRITE;
		}
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, area.x, area.y, area.width, area.height);
	}

	@Override
	void renderContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
		UIArea area = this.getRenderArea(tickDelta);
		int color = this.isEnabled() ? TEXT_COLOR : TEXT_COLOR_DISABLED;
		int textY = area.y + (area.height - 8) / 2 + (this.pressed ? 1 : 0);
		guiGraphics.centeredText(Minecraft.getInstance().font, this.text, area.x + area.width / 2, textY, color);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.isEnabled()) {
			return false;
		}
		boolean handled = super.mouseClicked(mouseX, mouseY, button);
		if (handled) {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			this.pressed = true;
		}
		return handled;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.pressed = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
}
