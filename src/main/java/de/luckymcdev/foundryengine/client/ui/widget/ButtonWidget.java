package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.UIVec;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class ButtonWidget extends PanelWidget {
    private final ClickEvent clickEvent;

    private int hoverColor = -1;

    public ButtonWidget(UIVec position, UIVec size, ClickEvent clickEvent) {
        super(position, size);
        this.clickEvent = clickEvent;
    }

    public int getHoverColor() {
        return this.hoverColor;
    }

    public <T extends ButtonWidget> T setHoverColor(int hoverColor) {
        this.hoverColor = hoverColor;
        return (T) this;
    }

    public <T extends ButtonWidget> T setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor.rgb();
        return (T) this;
    }

    @Override
    void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
        UIArea drawArea = this.getRenderArea(tickDelta);
        int drawColor = hoverColor != -1 && this.contains(mouseX, mouseY) ? hoverColor : backgroundColor;
        if (borderThickness > 0) {
            guiGraphics.fill(RenderPipelines.GUI, drawArea.x, drawArea.y, drawArea.x + drawArea.width, drawArea.y + drawArea.height, borderColor);
            UIArea inner = drawArea.shrink(borderThickness);
            guiGraphics.fill(RenderPipelines.GUI, inner.x, inner.y, inner.x + inner.width, inner.y + inner.height, drawColor);
        } else {
            guiGraphics.fill(RenderPipelines.GUI, drawArea.x, drawArea.y, drawArea.x + drawArea.width, drawArea.y + drawArea.height, drawColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean childClicked = super.mouseClicked(mouseX, mouseY, button);
        if (childClicked) return true;
        if (this.contains(mouseX, mouseY)) {
            this.clickEvent.onClick(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    public interface ClickEvent {
        void onClick(double mouseX, double mouseY, int button);
    }
}