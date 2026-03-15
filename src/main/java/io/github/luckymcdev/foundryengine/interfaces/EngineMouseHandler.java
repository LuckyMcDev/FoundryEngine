package io.github.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mouse Handler Extension
 */
public interface EngineMouseHandler {
    void tb$onMouseButton(long p_window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci);

    void tb$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci);
}
