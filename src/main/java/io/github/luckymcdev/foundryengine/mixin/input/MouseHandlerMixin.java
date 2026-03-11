package io.github.luckymcdev.foundryengine.mixin.input;

import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import io.github.luckymcdev.foundryengine.interfaces.EngineMouseHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * See {@link ImGuiManager#shouldInterceptMouse()}
 * Cancels Minecraft Mouse inputs if ImGui captures the Mouse.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin implements EngineMouseHandler {

    @Override
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    public void tb$onMouseButton(long p_window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptMouse()) {
            ci.cancel();
        }
    }

    @Override
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void tb$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptMouse()) {
            ci.cancel();
        }
    }
}

