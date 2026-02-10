package io.github.luckymcdev.mixin.input;

import io.github.luckymcdev.client.imgui.ImGuiHandler;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void vl$onKey(long p_window, int action, KeyEvent event, CallbackInfo ci) {
        if (ImGuiHandler.shouldInterceptKeyboard()) {
            ci.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void vl$onChar(long window, CharacterEvent event, CallbackInfo ci) {
        if (ImGuiHandler.shouldInterceptKeyboard()) {
            ci.cancel();
        }
    }
}
