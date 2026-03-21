package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Game Renderer Extension
 */
public interface EngineGameRenderer {
    void tb$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);

    void tb$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);
}
