package de.luckymcdev.foundryengine.client.editor.feature;

import de.luckymcdev.foundryengine.client.editor.EditorController;
import net.minecraft.client.Minecraft;

public abstract class DragEditorFeature implements EditorFeature {
    protected boolean wasUsing = false;
    protected int useTicks = 0;
    protected double storedDistance = 0;

    protected double getScrollSensitivity() {
        return 0.25;
    }

    @Override
    public void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        boolean using = EditorController.isUsingEditorItem();

        if (using && !wasUsing) {
            wasUsing = true;
            useTicks = 0;
            storedDistance = 0;
            onDragStart();
        }

        if (using) {
            useTicks++;
            onDragTick(mc);
        }

        if (!using && wasUsing) {
            wasUsing = false;
            onDragEnd();
            useTicks = 0;
        }
    }

    @Override
    public boolean onScroll(double vertical) {
        if (!wasUsing) return false;
        storedDistance = Math.max(storedDistance + (vertical * getScrollSensitivity()), 0);
        onDistanceChanged();
        return true;
    }

    @Override
    public void onDeactivated() {
        reset();
    }

    protected void reset() {
        wasUsing = false;
        useTicks = 0;
        storedDistance = 0;
    }

    protected abstract void onDragStart();

    protected abstract void onDragTick(Minecraft mc);

    protected abstract void onDragEnd();

    protected abstract void onDistanceChanged();
}
