package io.github.luckymcdev.interfaces;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface TbKeyboardHandler {
    void tb$onKey(long p_window, int action, KeyEvent event, CallbackInfo ci);
    void tb$onChar(long p_window, CharacterEvent event, CallbackInfo ci);
}
