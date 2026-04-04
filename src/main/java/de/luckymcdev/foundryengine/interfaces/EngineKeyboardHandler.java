package de.luckymcdev.foundryengine.interfaces;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keyboard Handler Extension
 */
public interface EngineKeyboardHandler extends EngineInterface<KeyboardHandler> {

    void engine$keyPress(long p_window, int action, KeyEvent event, CallbackInfo ci);

    void engine$charTyped(long p_window, CharacterEvent event, CallbackInfo ci);
}
