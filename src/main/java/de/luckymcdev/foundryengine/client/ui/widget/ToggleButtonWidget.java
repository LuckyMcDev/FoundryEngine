package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.UIVec;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class ToggleButtonWidget extends ButtonWidget {
    private Color toggledColor;

    private boolean pressed;

    public ToggleButtonWidget(UIVec position, UIVec size, ClickEvent clickEvent) {
        super(position, size, clickEvent);
    }

    public Color getToggledColor() {
        return this.toggledColor;
    }

    public <T extends ToggleButtonWidget> T setToggledColor(int toggledColor) {
        this.toggledColor = new Color(toggledColor);
        return (T) this;
    }

    public <T extends ToggleButtonWidget> T setToggledColor(Color toggledColor) {
        this.toggledColor = toggledColor;
        return (T) this;
    }

    public boolean isPressed() {
        return this.pressed;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean childClicked = super.mouseClicked(mouseX, mouseY, button);
        if (childClicked) return true;
        if (this.contains(mouseX, mouseY)) {
            this.pressed = !this.pressed;
            return true;
        }
        return false;
    }

    @Override
    void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
        UIArea drawArea = this.getRenderArea(tickDelta);
        Color drawColor = backgroundColor;
        if (this.isPressed()) drawColor = this.getToggledColor();
        if (this.contains(mouseX, mouseY)) drawColor = this.getHoverColor();
        if (borderThickness > 0) {
            guiGraphics.fill(RenderPipelines.GUI, drawArea.x, drawArea.y, drawArea.x + drawArea.width, drawArea.y + drawArea.height, borderColor.argb());
            UIArea inner = drawArea.shrink(2);
            guiGraphics.fill(RenderPipelines.GUI, inner.x, inner.y, inner.x + inner.width, inner.y + inner.height, drawColor.argb());
        } else {
            guiGraphics.fill(RenderPipelines.GUI, drawArea.x, drawArea.y, drawArea.x + drawArea.width, drawArea.y + drawArea.height, drawColor.argb());
        }
    }
}