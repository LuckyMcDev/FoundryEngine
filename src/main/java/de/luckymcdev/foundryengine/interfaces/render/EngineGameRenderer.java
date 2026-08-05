package de.luckymcdev.foundryengine.interfaces.render;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends the game renderer with pre- and post-render hooks for head and return rendering.
 */
public interface EngineGameRenderer extends EngineInterface<GameRenderer> {
	/**
	 * Called before the main render pass to allow custom head rendering.
	 */
	default void engine$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		throw new NoMixinException(this);
	}

	/**
	 * Called after the main render pass to allow custom post-rendering.
	 */
	default void engine$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		throw new NoMixinException(this);
	}
}
