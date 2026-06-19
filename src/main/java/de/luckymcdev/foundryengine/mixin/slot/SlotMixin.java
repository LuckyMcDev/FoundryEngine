package de.luckymcdev.foundryengine.mixin.slot;

import com.google.common.collect.Lists;
import de.luckymcdev.foundryengine.common.slot.SlotCustomization;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Slot.class)
public class SlotMixin implements SlotCustomization {

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
		if (!this.engine$disabledOverride) return;
		cir.setReturnValue(false);
	}

	@Override
	public void engine$setX(int x) {
		this.x = x;
	}

	@Override
	public void engine$setY(int y) {
		this.y = y;
	}

	@Override
	public void engine$setDisabledOverride(boolean disabled) {
		this.engine$disabledOverride = disabled;
	}

	@Override
	public boolean engine$getDisabledOverride() {
		return this.engine$disabledOverride;
	}

	@Override
	public void engine$setSlotTooltipText(List<Component> newSlotTooltipTextList) {
		this.engine$slotTooltipText = newSlotTooltipTextList;
	}

	@Override
	public List<Component> engine$getSlotTooltipText() {
		return this.engine$slotTooltipText;
	}
}
