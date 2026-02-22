package io.github.luckymcdev.foundryengine.mixin.input;

import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import io.github.luckymcdev.foundryengine.interfaces.TbKeyboardHandler;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

/**
 * See {@link ImGuiManager#shouldInterceptKeyboard()}
 * Cancels Minecraft Keyboard inputs if ImGui captures the keyboard.
 */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin implements TbKeyboardHandler {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void fe$keyPress(long p_window, int action, KeyEvent event, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptKeyboard()) {
            ci.cancel();
        }
        if (p_window == Client.getWindow().handle() && action == GLFW_PRESS && Client.EDITOR_KEY.mapping().matches(event)) {
            Client.getImGuiManager().toggle();
        }
    }

    @Override
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void fe$charTyped(long p_window, CharacterEvent event, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptKeyboard()) {
            ci.cancel();
        }
    }
}
