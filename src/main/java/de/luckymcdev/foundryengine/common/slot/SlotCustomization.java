package de.luckymcdev.foundryengine.common.slot;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface SlotCustomization {
    void engine$setX(int x);

    void engine$setY(int y);

    void engine$setDisabledOverride(boolean disabled);

    boolean engine$getDisabledOverride();

    void engine$setSlotTooltipText(List<Component> newSlotTooltipTextList);

    List<Component> engine$getSlotTooltipText();

    default void lock() {
        engine$setDisabledOverride(true);
    }

    default void unlock() {
        engine$setDisabledOverride(false);
    }

    default boolean isLocked() {
        return engine$getDisabledOverride();
    }

    default void setTooltip(List<Component> tooltip) {
        engine$setSlotTooltipText(tooltip);
    }

    default List<Component> getTooltip() {
        return engine$getSlotTooltipText();
    }
}
