package de.luckymcdev.foundryengine.mixin.slot;

import com.google.common.collect.Lists;
import de.luckymcdev.foundryengine.interfaces.slot.EngineSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Implements {@link EngineSlot} on Slot to support custom tooltips and disabled override.
 */
@Mixin(Slot.class)
public class SlotMixin implements EngineSlot {

	@Shadow
	@Final
	@Mutable
	public int x;

	@Shadow
	@Final
	@Mutable
	public int y;

	@Unique
	private List<Component> engine$slotTooltipText = Lists.newArrayList();

	@Unique
	private boolean engine$disabledOverride = false;

	@Inject(method = "isActive", at = @At("TAIL"), cancellable = true)
	private void engine$isActive(CallbackInfoReturnable<Boolean> cir) {
		if (!this.engine$disabledOverride) {
			return;
		}
		cir.setReturnValue(false);
	}

	/**
	 * Sets the X position of this slot.
	 */
	@Override
	public void engine$setX(int x) {
		this.x = x;
	}

	/**
	 * Sets the Y position of this slot.
	 */
	@Override
	public void engine$setY(int y) {
		this.y = y;
	}

	/**
	 * Overrides the disabled state of this slot.
	 */
	@Override
	public void engine$setDisabledOverride(boolean disabled) {
		this.engine$disabledOverride = disabled;
	}

	/**
	 * Returns whether this slot is overridden as disabled.
	 */
	@Override
	public boolean engine$getDisabledOverride() {
		return this.engine$disabledOverride;
	}

	/**
	 * Sets the custom tooltip text for this slot.
	 */
	@Override
	public void engine$setSlotTooltipText(List<Component> newSlotTooltipTextList) {
		this.engine$slotTooltipText = newSlotTooltipTextList;
	}

	/**
	 * Returns the custom tooltip text for this slot.
	 */
	@Override
	public List<Component> engine$getSlotTooltipText() {
		return this.engine$slotTooltipText;
	}
}
