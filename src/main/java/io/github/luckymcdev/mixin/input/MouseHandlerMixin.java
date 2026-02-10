package io.github.luckymcdev.mixin.input;

import io.github.luckymcdev.client.imgui.ImGuiHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    public void tb$onMouseButton(long p_window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (ImGuiHandler.shouldInterceptMouse()) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void tb$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ImGuiHandler.shouldInterceptMouse()) {
            ci.cancel();
        }
    }
}

