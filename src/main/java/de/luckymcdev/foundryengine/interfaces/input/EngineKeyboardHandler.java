package de.luckymcdev.foundryengine.interfaces.input;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
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
	default void engine$keyPress(long p_window, int action, KeyEvent event, CallbackInfo ci) {
		throw new NoMixinException(this);
	}

	/**
	 * Called on character typed events to allow interception or augmentation.
	 */
	default void engine$charTyped(long p_window, CharacterEvent event, CallbackInfo ci) {
		throw new NoMixinException(this);
	}
}
