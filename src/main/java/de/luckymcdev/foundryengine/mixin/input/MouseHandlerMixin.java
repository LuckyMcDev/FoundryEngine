package de.luckymcdev.foundryengine.mixin.input;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.interfaces.EngineMouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * See {@link de.luckymcdev.foundryengine.client.imgui.ImGuiManager#shouldInterceptMouse()}
 * Cancels Minecraft Mouse inputs if ImGui captures the Mouse.
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin implements EngineMouseHandler {

    /**
     * Cancels mouse button events when ImGui captures the mouse.
     */
    @Override
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    public void engine$onButton(long handle, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptMouse()) {
            ci.cancel();
        }
    }

    /**
     * Cancels scroll events when ImGui captures the mouse, or forwards to the in-world editor.
     */
    @Override
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void engine$onScroll(long handle, double horizontal, double vertical, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptMouse()) {
            ci.cancel();
            return;
        }

        // In-world editor uses scroll to push/pull points.
        if (Minecraft.getInstance().screen == null && Client.getEditorController().onScroll(vertical)) {
            ci.cancel();
        }
    }
}

