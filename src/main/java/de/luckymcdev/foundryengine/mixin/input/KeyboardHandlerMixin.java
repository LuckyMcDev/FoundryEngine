package de.luckymcdev.foundryengine.mixin.input;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.EditorScreen;
import de.luckymcdev.foundryengine.interfaces.input.EngineKeyboardHandler;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

/**
 * Blocks game input while a docked window fully covers the game view
 * (see {@link de.luckymcdev.foundryengine.client.imgui.ImGuiManager#shouldBlockInput()}).
 * Input that ImGui itself captures is already cancelled by ImGuiMC's input mixins.
 */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin implements EngineKeyboardHandler {

	/**
	 * Cancels keyboard events when ImGui captures the keyboard, and handles editor/menu toggle hotkeys.
	 */
	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	public void engine$keyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
		if (Client.getImGuiManager().shouldBlockInput()) {
			ci.cancel();
			return;
		}

		if (handle == Minecraft.getInstance().getWindow().handle() && action == GLFW_PRESS && Client.MENU_BAR_KEY.matches(event)) {
			Client.getImGuiManager().toggleMenuBar();
		}

		if (handle == Minecraft.getInstance().getWindow().handle() && action == GLFW_PRESS && Client.EDITOR_KEY.matches(event)) {
			if (event.hasControlDown()) {
				Client.getImGuiManager().enable();
				Client.setScreen(new EditorScreen(true));
			} else {
				Client.getImGuiManager().toggle();
			}
		}
	}

	/**
	 * Cancels char-typed events while the dock blocks game input.
	 */
	@Override
	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	public void engine$charTyped(long handle, CharacterEvent event, CallbackInfo ci) {
		if (Client.getImGuiManager().shouldBlockInput()) {
			ci.cancel();
		}
	}
}
