package io.github.luckymcdev.foundryengine.mixin.input;

import io.github.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import io.github.luckymcdev.foundryengine.common.Instances;
import io.github.luckymcdev.foundryengine.interfaces.TbKeyboardHandler;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin implements TbKeyboardHandler {

    @Override
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void tb$onKey(long p_window, int action, KeyEvent event, CallbackInfo ci) {
        if (Instances.getImGuiManager().shouldInterceptKeyboard()) {
            ci.cancel();
        }
    }

    @Override
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void tb$onChar(long p_window, CharacterEvent event, CallbackInfo ci) {
        if (Instances.getImGuiManager().shouldInterceptKeyboard()) {
            ci.cancel();
        }
    }
}
