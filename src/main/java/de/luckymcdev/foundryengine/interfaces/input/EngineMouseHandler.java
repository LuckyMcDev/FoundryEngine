package de.luckymcdev.foundryengine.interfaces.input;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends mouse input handling with button and scroll event hooks.
 */
public interface EngineMouseHandler extends EngineInterface<MouseHandler> {
	/**
	 * Called on mouse button events to allow interception or augmentation.
	 */
	default void engine$onButton(long p_window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
		throw new NoMixinException(this);
	}

	/**
	 * Called on mouse scroll events to allow interception or augmentation.
	 */
	default void engine$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		throw new NoMixinException(this);
	}

	/**
	 * Called on mouse move events to allow interception or augmentation.
	 */
	default void engine$onMove(long window, double x, double y, CallbackInfo ci) {
		throw new NoMixinException(this);
	}

	/**
	 * Resets internal mouse state and positions the cursor off-screen.
	 */
	default void engine$resetMouse() {
		throw new NoMixinException(this);
	}
}
