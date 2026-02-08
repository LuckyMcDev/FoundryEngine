package io.github.luckymcdev.mixin.imgui.input;

import io.github.luckymcdev.client.imgui.ImGuiImpl;
import io.github.luckymcdev.interfaces.TbWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    public void tb$onMouseButton(long p_window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (ImGuiImpl.shouldInterceptMouse()) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void tb$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ImGuiImpl.shouldInterceptMouse()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "onMove", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public double tb$modifyCursorX(double x) {
        TbWindow window = (TbWindow) (Object) Minecraft.getInstance().getWindow();
        return window.tb$modifyCursorX(x);
    }

    @ModifyVariable(method = "onMove", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    public double tb$modifyCursorY(double y) {
        TbWindow window = (TbWindow) (Object) Minecraft.getInstance().getWindow();
        return window.tb$modifyCursorY(y);
    }
}

