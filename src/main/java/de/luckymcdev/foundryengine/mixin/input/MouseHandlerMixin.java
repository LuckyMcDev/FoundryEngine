package de.luckymcdev.foundryengine.mixin.input;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.interfaces.input.EngineMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks game input while a docked window fully covers the game view
 * (see {@link de.luckymcdev.foundryengine.client.imgui.ImGuiManager#shouldBlockInput()}).
 * Input that ImGui itself captures is already cancelled by ImGuiMC's input mixins.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin implements EngineMouseHandler {
	@Shadow
	private double xpos;
	@Shadow
	private double ypos;

	/**
	 * Cancels mouse button events while the dock blocks game input.
	 */
	@Override
	@Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
	public void engine$onButton(long handle, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
		if (Client.getImGuiManager().shouldBlockInput()) {
			ci.cancel();
		}
	}

	/**
	 * Cancels scroll events while the dock blocks game input, or forwards to the in-world editor.
	 */
	@Override
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	public void engine$onScroll(long handle, double horizontal, double vertical, CallbackInfo ci) {
		if (Client.getImGuiManager().shouldBlockInput()) {
			ci.cancel();
			return;
		}

		// In-world editor uses scroll to push/pull points.
		//? if 26.1 {
		if (Minecraft.getInstance().screen == null && Client.getEditorController().onScroll(vertical)) {
		 //?} else {
		/*if (Minecraft.getInstance().gui.screen() == null && Client.getEditorController().onScroll(vertical)) {
			*///?}
			ci.cancel();
		}
	}

	/**
	 * Cancels cursor move events while the dock blocks game input, resetting position off-screen
	 * so MC screen widgets don't receive hover/click events at the real cursor position.
	 */
	@Override
	@Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
	public void engine$onMove(long handle, double xpos, double ypos, CallbackInfo ci) {
		if (Client.getImGuiManager().shouldBlockInput()) {
			this.xpos = -1.0;
			this.ypos = -1.0;
			ci.cancel();
		}
	}

	@Override
	public void engine$resetMouse() {
		xpos = -1.0;
		ypos = -1.0;
	}
}
