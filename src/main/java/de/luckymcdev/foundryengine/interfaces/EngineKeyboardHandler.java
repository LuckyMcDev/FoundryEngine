package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends keyboard input handling with key press and character typed hooks.
 */
public interface EngineKeyboardHandler extends EngineInterface<KeyboardHandler> {
	/**
	 * Called on key press events to allow interception or augmentation.
	 */
	void engine$keyPress(long p_window, int action, KeyEvent event, CallbackInfo ci);

	/**
	 * Called on character typed events to allow interception or augmentation.
	 */
	void engine$charTyped(long p_window, CharacterEvent event, CallbackInfo ci);
}
