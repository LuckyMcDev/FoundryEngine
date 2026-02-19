package io.github.luckymcdev.foundryengine.mixin.input;

import io.github.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import io.github.luckymcdev.foundryengine.common.Instances;
import io.github.luckymcdev.foundryengine.interfaces.TbMouseHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin implements TbMouseHandler {

    @Override
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    public void tb$onMouseButton(long p_window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (Instances.getImGuiManager().shouldInterceptMouse()) {
            ci.cancel();
        }
    }

    @Override
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void tb$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (Instances.getImGuiManager().shouldInterceptMouse()) {
            ci.cancel();
        }
    }
}

