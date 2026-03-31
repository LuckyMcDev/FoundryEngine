package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.UIVec;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class MaskWidget extends WidgetBase {
    public MaskWidget(UIVec position, UIVec size) {
        super(position, size);
    }

    @Override
    public void preRenderChild(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        UIArea area = this.getArea();
        guiGraphics.pose().pushMatrix();
        guiGraphics.enableScissor(area.x, area.y, area.x + area.width, area.y + area.height);
    }

    @Override
    public void postRenderChild(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        guiGraphics.disableScissor();
        guiGraphics.pose().popMatrix();
    }
}