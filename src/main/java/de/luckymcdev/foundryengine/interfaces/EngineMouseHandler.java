package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mouse Handler Extension
 */
public interface EngineMouseHandler extends EngineInterface<MouseHandler> {

    void engine$onButton(long p_window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci);

    void engine$onScroll(long window, double horizontal, double vertical, CallbackInfo ci);
}
