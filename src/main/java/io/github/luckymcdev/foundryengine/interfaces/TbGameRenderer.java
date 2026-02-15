package io.github.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface TbGameRenderer {
    void tb$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);
    void tb$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci);
}
