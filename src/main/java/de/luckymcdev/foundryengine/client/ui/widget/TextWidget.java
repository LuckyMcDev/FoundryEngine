package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.Enums;
import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.UIVec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TextWidget extends WidgetBase {
	private MutableComponent text = Component.empty();
	private double fontSize = 9;
	private Enums.Alignment alignment = Enums.Alignment.TOP_LEFT;

	public TextWidget(UIVec position, UIVec size) {
		super(position, size);
	}

	public MutableComponent getText() {
		return this.text;
	}

	public <T extends TextWidget> T setText(@NotNull MutableComponent text) {
		this.text = text;
		return (T) this;
	}

	public double getFontSize() {
		return this.fontSize;
	}

	public <T extends TextWidget> T setFontSize(double fontSize) {
		this.fontSize = fontSize;
		return (T) this;
	}

	public Enums.Alignment getAlignment() {
		return this.alignment;
	}

	public <T extends TextWidget> T setAlignment(@NotNull Enums.Alignment alignment) {
		this.alignment = alignment;
		return (T) this;
	}

	@Override
	void renderContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
		UIArea drawArea = this.getRenderArea(tickDelta);
		List<FormattedCharSequence> wrapped = Minecraft.getInstance().font.split(this.text, drawArea.width);
		double yOff = 0;
		if (alignment.getYAlignment() == Enums.YAlignment.MIDDLE) {
			yOff = drawArea.height / 2d - fontSize * wrapped.size() / 2;
		}
		if (alignment.getYAlignment() == Enums.YAlignment.BOTTOM) {
			yOff = drawArea.height - fontSize * wrapped.size();
		}
		for (FormattedCharSequence line : wrapped) {
			guiGraphics.pose().pushMatrix();
			int lineWidth = Minecraft.getInstance().font.width(line);
			switch (alignment.getXAlignment()) {
				case LEFT -> {
					guiGraphics.pose().translate(drawArea.x, drawArea.y + (float) yOff);
					guiGraphics.pose().scale((float) fontSize / 9f, (float) fontSize / 9f);
				}
				case CENTER -> {
					guiGraphics.pose().translate(drawArea.x + (drawArea.width - lineWidth) / 2f, drawArea.y + (float) yOff);
					guiGraphics.pose().scale((float) fontSize / 9f, (float) fontSize / 9f);
				}
				case RIGHT -> {
					guiGraphics.pose().translate(drawArea.x + drawArea.width - lineWidth, drawArea.y + (float) yOff);
					guiGraphics.pose().scale((float) fontSize / 9f, (float) fontSize / 9f);
				}
			}
			if (debug) {
				guiGraphics.fill(RenderPipelines.GUI, 0, 0, lineWidth, Mth.ceil(fontSize), 0xFF000000);
			}
			guiGraphics.text(Minecraft.getInstance().font, line, 0, 0, 0xFFFFFFFF, false);
			guiGraphics.pose().popMatrix();
			yOff += fontSize;
		}
	}
}