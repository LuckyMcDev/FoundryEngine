package io.github.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TODO: Rename
 * Keyboard Handler Extension
 */
public interface EngineKeyboardHandler {
    void fe$keyPress(long p_window, int action, KeyEvent event, CallbackInfo ci);

    void fe$charTyped(long p_window, CharacterEvent event, CallbackInfo ci);
}
