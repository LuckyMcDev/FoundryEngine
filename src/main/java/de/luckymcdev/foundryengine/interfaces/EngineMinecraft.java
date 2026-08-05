package de.luckymcdev.foundryengine.interfaces;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.gizmos.SimpleGizmoCollector;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends the Minecraft client with init, close, and per-tick gizmo hooks.
 */
public interface EngineMinecraft extends EngineInterface<Minecraft> {
	/**
	 * Called during Minecraft initialization to allow custom setup.
	 */
	default void engine$init(GameConfig gameConfig, CallbackInfo ci) {
		throw new NoMixinException(this);
	}

	/**
	 * Called on Minecraft shutdown to allow cleanup.
	 */
	default void engine$close(CallbackInfo ci) {
		throw new NoMixinException(this);
	}

	/**
	 * Returns the per-tick gizmo collector for debug rendering.
	 */
	default SimpleGizmoCollector engine$perTickGizmos() {
		throw new NoMixinException(this);
	}
}
