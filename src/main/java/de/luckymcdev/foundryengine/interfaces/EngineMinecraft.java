package de.luckymcdev.foundryengine.interfaces;

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
	void engine$init(GameConfig gameConfig, CallbackInfo ci);

	/**
	 * Called on Minecraft shutdown to allow cleanup.
	 */
	void engine$close(CallbackInfo ci);

	/**
	 * Returns the per-tick gizmo collector for debug rendering.
	 */
	SimpleGizmoCollector engine$perTickGizmos();
}
