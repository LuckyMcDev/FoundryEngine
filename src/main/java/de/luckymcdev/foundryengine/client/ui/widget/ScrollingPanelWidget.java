package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.Enums;
import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.UIVec;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import org.joml.Vector2i;

public class ScrollingPanelWidget extends PanelWidget {
    private double scrollX = 0;
    private double scrollY = 0;

    private UIArea scrollLimits = null;
    private Vector2i scrollPadding = null;
    private boolean autoScrollLimits = false;

    private boolean allowDragging = false;
    private boolean allowMouseWheel = true;

    private Enums.MouseButton dragButton = Enums.MouseButton.LEFT;
    private double scrollSensitivity = 1;
    private boolean startedDrag = false;

    public ScrollingPanelWidget(UIVec position, UIVec size) {
        super(position, size);
    }

    public <T extends ScrollingPanelWidget> T enableDragging() {
        this.allowDragging = true;
        return (T) this;
    }

    public <T extends ScrollingPanelWidget> T disableDragging() {
        this.allowDragging = false;
        return (T) this;
    }

    public <T extends ScrollingPanelWidget> T enableMouseWheel() {
        this.allowMouseWheel = true;
        return (T) this;
    }

    public <T extends ScrollingPanelWidget> T disableMouseWheel() {
        this.allowMouseWheel = false;
        return (T) this;
    }

    public double getScrollSensitivity() {
        return this.scrollSensitivity;
    }

    public <T extends ScrollingPanelWidget> T setScrollSensitivity(double scrollSensitivity) {
        this.scrollSensitivity = scrollSensitivity;
        return (T) this;
    }

    public Enums.MouseButton getDragButton() {
        return this.dragButton;
    }

    public <T extends ScrollingPanelWidget> T setDragButton(Enums.MouseButton dragButton) {
        this.dragButton = dragButton;
        return (T) this;
    }

    public <T extends ScrollingPanelWidget> T setScrollLimits(int paddingX, int paddingY) {
        this.scrollPadding = new Vector2i(paddingX, paddingY);
        this.autoScrollLimits = true;
        return (T) this;
    }

    public UIArea getScrollLimits() {
        return this.scrollLimits;
    }

    public <T extends ScrollingPanelWidget> T setScrollLimits(UIArea scrollLimits) {
        this.scrollLimits = scrollLimits;
        this.autoScrollLimits = false;
        return (T) this;
    }

    @Override
    public void onInit() {
        super.onInit();
    }

    @Override
    public void updateArea(UIArea parentArea) {
        super.updateArea(parentArea);
        this.recalculateScrollLimits();
    }

    private void recalculateScrollLimits() {
        if (!this.autoScrollLimits) return;
        UIArea area = this.getArea();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (WidgetBase child : this.getChildren()) {
            UIArea subArea = child.getBaseArea();
            minX = Math.min(minX, subArea.x - area.x - scrollPadding.x);
            minY = Math.min(minY, subArea.y - area.y - scrollPadding.y);
            maxX = Math.max(maxX, subArea.x - area.x + subArea.width - area.width + (borderThickness * 2) + scrollPadding.x);
            maxY = Math.max(maxY, subArea.y - area.y + subArea.height - area.height + (borderThickness * 2) + scrollPadding.y);
        }
        this.scrollLimits = new UIArea(Math.min(0, minX), Math.min(0, minY), maxX - Math.min(0, minX), maxY - Math.min(0, minY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean childClicked = super.mouseClicked(mouseX, mouseY, button);
        if (childClicked) return true;
        if (this.contains(mouseX, mouseY) && button == dragButton.button() && allowDragging) {
            startedDrag = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean childReleased = super.mouseReleased(mouseX, mouseY, button);
        if (childReleased) return true;
        startedDrag = false;
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (allowDragging && startedDrag && button == dragButton.button()) {
            doScroll(dx, dy);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean childScrolled = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (childScrolled) return true;
        if (allowMouseWheel && this.contains(mouseX, mouseY)) {
            doScroll(scrollX * scrollSensitivity, scrollY * scrollSensitivity);
            return true;
        }
        return false;
    }

    private void doScroll(double dx, double dy) {
        this.scrollX -= dx;
        this.scrollY -= dy;
        recalculateScrollLimits();
        if (this.scrollLimits != null) {
            this.scrollX = Mth.clamp(this.scrollX, this.scrollLimits.x, this.scrollLimits.x + this.scrollLimits.width);
            this.scrollY = Mth.clamp(this.scrollY, this.scrollLimits.y, this.scrollLimits.y + this.scrollLimits.height);
        }
        for (WidgetBase child : this.getChildren()) {
            child.setOffset(new Vector2i(-(int) this.scrollX, -(int) this.scrollY));
        }
    }

    @Override
    public void preRenderChild(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        UIArea area = this.getArea();
        guiGraphics.pose().pushMatrix();
        guiGraphics.enableScissor(area.x + borderThickness, area.y + borderThickness,
                area.x + area.width - borderThickness, area.y + area.height - borderThickness);
    }

    @Override
    public void postRenderChild(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        guiGraphics.disableScissor();
        guiGraphics.pose().popMatrix();
    }
}