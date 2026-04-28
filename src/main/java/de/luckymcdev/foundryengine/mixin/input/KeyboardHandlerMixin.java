package de.luckymcdev.foundryengine.mixin.input;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.EditorScreen;
import de.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import de.luckymcdev.foundryengine.interfaces.EngineKeyboardHandler;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
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
public class KeyboardHandlerMixin implements EngineKeyboardHandler {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void engine$keyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptKeyboard()) {
            ci.cancel();
            return;
        }

        if (handle == Client.getWindow().handle() && action == GLFW_PRESS && Client.EDITOR_KEY.mapping().matches(event)) {
            if (event.hasControlDown()) {
                Client.getImGuiManager().enable();
                Minecraft.getInstance().setScreen(new EditorScreen(true));
            } else {
                Client.getImGuiManager().toggle();
            }
        }
    }

    @Override
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void engine$charTyped(long handle, CharacterEvent event, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptKeyboard()) {
            ci.cancel();
        }
    }
}
