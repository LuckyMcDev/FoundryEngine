package de.luckymcdev.foundryengine.mixin.screen;

import de.luckymcdev.foundryengine.common.slot.SlotCustomization;
import de.luckymcdev.foundryengine.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

/**
 * Renders custom slot tooltips on inventory screens via SlotCustomization.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {

	@Shadow
	@Final
	protected T menu;

	@Shadow
	@Nullable
	protected Slot hoveredSlot;

	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	/**
	 * Injects at tail of extractTooltip to render custom slot tooltips.
	 */
	@Inject(method = "extractTooltip", at = @At("RETURN"))
	protected void engine$renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
		if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && !this.hoveredSlot.hasItem()) {
			if (ClientConfig.SHOW_SLOT_TOOLTIP.getAsBoolean() && this.hoveredSlot instanceof SlotCustomization slotWithTooltip) {
				List<Component> list = slotWithTooltip.engine$getSlotTooltipText();
				if (!list.isEmpty()) {
					graphics.setTooltipForNextFrame(this.font, list, Optional.empty(), mouseX, mouseY);
				}
			}
		}
	}

}
