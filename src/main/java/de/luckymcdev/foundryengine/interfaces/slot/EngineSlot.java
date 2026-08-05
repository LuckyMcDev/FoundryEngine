package de.luckymcdev.foundryengine.interfaces.slot;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * Customizes inventory slot behavior (position, locking, tooltips).
 */
public interface EngineSlot extends EngineInterface<Slot> {
	/**
	 * Sets the X position of this slot.
	 */
	default void engine$setX(int x) {
		throw new NoMixinException(this);
	}

	/**
	 * Sets the Y position of this slot.
	 */
	default void engine$setY(int y) {
		throw new NoMixinException(this);
	}

	/**
	 * Overrides the disabled state of this slot.
	 */
	default void engine$setDisabledOverride(boolean disabled) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns whether this slot is overridden as disabled.
	 */
	default boolean engine$getDisabledOverride() {
		throw new NoMixinException(this);
	}

	/**
	 * Sets the custom tooltip text for this slot.
	 */
	default void engine$setSlotTooltipText(List<Component> tooltip) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns the custom tooltip text for this slot.
	 */
	default List<Component> engine$getSlotTooltipText() {
		throw new NoMixinException(this);
	}

	default void lock() {
		engine$setDisabledOverride(true);
	}

	default void unlock() {
		engine$setDisabledOverride(false);
	}

	default boolean isLocked() {
		return engine$getDisabledOverride();
	}

	default List<Component> getTooltip() {
		return engine$getSlotTooltipText();
	}

	default void setTooltip(List<Component> tooltip) {
		engine$setSlotTooltipText(tooltip);
	}
}
