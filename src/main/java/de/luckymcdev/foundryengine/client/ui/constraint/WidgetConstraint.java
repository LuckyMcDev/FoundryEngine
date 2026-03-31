package de.luckymcdev.foundryengine.client.ui.constraint;

import de.luckymcdev.foundryengine.client.ui.widget.WidgetBase;

public class WidgetConstraint {
    WidgetBase parent;

    public WidgetConstraint() {
    }

    public WidgetBase getParent() {
        return this.parent;
    }

    public <T extends WidgetConstraint> T setParent(WidgetBase parent) {
        this.parent = parent;
        return (T) this;
    }

    public void update() {
    }
}
