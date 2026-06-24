package de.luckymcdev.foundryengine.mixin.screen;

import com.google.common.collect.Lists;
import de.luckymcdev.foundryengine.common.slot.SlotCustomization;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Implements {@link SlotCustomization} on the creative inventory slot wrapper.
 */
@Mixin(targets = {"net/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$SlotWrapper"})
public abstract class CreativeModeInventoryScreenSlotWrapperMixin extends Slot implements SlotCustomization {

    @Unique
    private List<Component> engine$slotTooltipText = Lists.newArrayList();

    @Unique
    private boolean engine$disabledOverride = false;

    public CreativeModeInventoryScreenSlotWrapperMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    /**
     * Copies tooltip and disabled state from the wrapped slot.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    public void CreativeModeInventoryScreen$SlotWrapper(Slot target, int index, int x, int y, CallbackInfo ci) {
        var targetCus = (SlotCustomization) target;
        this.engine$setSlotTooltipText(targetCus.engine$getSlotTooltipText());
        this.engine$setDisabledOverride(targetCus.engine$getDisabledOverride());
    }

    @Inject(method = "isActive", at = @At("TAIL"), cancellable = true)
    private void engine$isActive(CallbackInfoReturnable<Boolean> cir) {
        if (!this.engine$disabledOverride) return;
        cir.setReturnValue(false);
    }

    /**
     * Sets the disabled override for this slot.
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
