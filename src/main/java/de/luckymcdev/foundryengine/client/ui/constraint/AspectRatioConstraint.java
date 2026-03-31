package de.luckymcdev.foundryengine.client.ui.constraint;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.UIVec;

public class AspectRatioConstraint extends WidgetConstraint {
    double ratio;
    boolean prioritizeHeight;

    public AspectRatioConstraint(double ratio, boolean prioritizeHeight) {
        this.ratio = ratio;
        this.prioritizeHeight = prioritizeHeight;
    }

    @Override
    public void update() {
        if (this.getParent() == null) return;
        UIArea area = this.getParent().getArea();
        int targetWidth;
        int targetHeight;
        if (prioritizeHeight) {
            targetHeight = area.height;
            targetWidth = (int) (ratio * targetHeight);
        } else {
            targetWidth = area.width;
            targetHeight = (int) (targetWidth / ratio);
        }
        if (this.getParent().getSize().offsetX == targetWidth && this.getParent().getSize().offsetY == targetHeight) {
            return;
        }
        this.getParent().setSize(new UIVec(0, 0, targetWidth, targetHeight));
    }
}
