package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.common.util.color.Color;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.style.LengthPercentage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class PanelWidget extends WidgetBase {
	Color backgroundColor = Color.BLACK;
	Color borderColor = Color.WHITE;
	int borderThickness = 0;

	public PanelWidget() {
		super();
	}

	public Color getBackgroundColor() {
		return this.backgroundColor;
	}

	public <T extends PanelWidget> T setBackgroundColor(int backgroundColor) {
		this.backgroundColor = new Color(backgroundColor);
		return (T) this;
	}

	public <T extends PanelWidget> T setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		return (T) this;
	}

	public <T extends PanelWidget> T setBorder(int borderColor, int borderThickness) {
		this.setBorderColor(borderColor);
		this.setBorderThickness(borderThickness);
		return (T) this;
	}

	public <T extends PanelWidget> T setBorder(Color borderColor, int borderThickness) {
		this.setBorderColor(borderColor);
		this.setBorderThickness(borderThickness);
		return (T) this;
	}

	public Color getBorderColor() {
		return this.borderColor;
	}

	public <T extends PanelWidget> T setBorderColor(int borderColor) {
		this.borderColor = new Color(borderColor);
		return (T) this;
	}

	public <T extends PanelWidget> T setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
		return (T) this;
	}

	public int getBorderThickness() {
		return this.borderThickness;
	}

	public <T extends PanelWidget> T setBorderThickness(int borderThickness) {
		this.borderThickness = borderThickness;
		this.style.border = TaffyRect.all(LengthPercentage.length(borderThickness));
		return (T) this;
	}

	@Override
	void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
		UIArea drawArea = this.getRenderArea(tickDelta);
		if (borderThickness > 0) {
			guiGraphics.fill(RenderPipelines.GUI, drawArea.x, drawArea.y, drawArea.x + drawArea.width, drawArea.y + drawArea.height, borderColor.argb());
			UIArea inner = drawArea.shrink(borderThickness);
			guiGraphics.fill(RenderPipelines.GUI, inner.x, inner.y, inner.x + inner.width, inner.y + inner.height, backgroundColor.argb());
		} else {
			guiGraphics.fill(RenderPipelines.GUI, drawArea.x, drawArea.y, drawArea.x + drawArea.width, drawArea.y + drawArea.height, backgroundColor.argb());
		}
	}
}