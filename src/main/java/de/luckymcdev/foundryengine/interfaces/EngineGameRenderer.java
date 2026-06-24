package de.luckymcdev.foundryengine.interfaces;

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
    void engine$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);

    /**
     * Called after the main render pass to allow custom post-rendering.
     */
    void engine$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);
}
