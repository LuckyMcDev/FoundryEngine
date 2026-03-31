package de.luckymcdev.foundryengine.client.ui.layout;

import de.luckymcdev.foundryengine.client.ui.widget.WidgetBase;

public abstract class WidgetLayout {
    WidgetBase parent;

    public WidgetLayout() {
    }

    public WidgetBase getParent() {
        return this.parent;
    }

    public <T extends WidgetLayout> T setParent(WidgetBase parent) {
        this.parent = parent;
        return (T) this;
    }

    public void update() {
    }
}
