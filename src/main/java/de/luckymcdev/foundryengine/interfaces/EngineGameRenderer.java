package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Game Renderer Extension
 */
public interface EngineGameRenderer extends EngineInterface<GameRenderer> {
    void engine$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);

    void engine$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);
}
