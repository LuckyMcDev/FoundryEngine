package io.github.luckymcdev.foundryengine.client.imgui.backend;

import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.glfw.ImGuiImplGlfw;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Extension to {@link ImGuiImplGlfw} which makes the Clipboard be handled by Minecraft.
 */
@ApiStatus.Internal
public class FeImGuiImplGlfw extends ImGuiImplGlfw {

    @Override
    protected ImStrSupplier getClipboardTextFn() {
        return new ImStrSupplier() {
            @Override
            public String get() {
                long window = Minecraft.getInstance().getWindow().handle();
                if (window != 0) {
                    return Minecraft.getInstance().keyboardHandler.getClipboard();
                }

                final String clipboardString = glfwGetClipboardString(window);
                return clipboardString != null ? clipboardString : "";
            }
        };
    }

    @Override
    protected ImStrConsumer setClipboardTextFn() {
        return new ImStrConsumer() {
            @Override
            public void accept(final String text) {
                long window = Minecraft.getInstance().getWindow().handle();
                if (window != 0) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(text);
                } else {
                    glfwSetClipboardString(window, text);
                }
            }
        };
    }

    @Override
    public void keyCallback(long window, int keycode, int scancode, int action, int mods) {
        if (action != GLFW_PRESS && action != GLFW_RELEASE) {
            return;
        }

        // Workaround: X11 does not include the current pressed/released modifier key in 'mods'.
        // https://github.com/glfw/glfw/issues/1630
        int keyModifiers = mods;
        final int keycodeToMod = keyToModifier(keycode);
        if (keycodeToMod != 0) {
            keyModifiers = (action == GLFW_PRESS) ? (mods | keycodeToMod) : (mods & ~keycodeToMod);
        }

        // Delegate to the base implementation with the corrected mods
        super.keyCallback(window, keycode, scancode, action, keyModifiers);
    }

    private int keyToModifier(final int key) {
        if (key == GLFW_KEY_LEFT_CONTROL || key == GLFW_KEY_RIGHT_CONTROL) return GLFW_MOD_CONTROL;
        if (key == GLFW_KEY_LEFT_SHIFT || key == GLFW_KEY_RIGHT_SHIFT) return GLFW_MOD_SHIFT;
        if (key == GLFW_KEY_LEFT_ALT || key == GLFW_KEY_RIGHT_ALT) return GLFW_MOD_ALT;
        if (key == GLFW_KEY_LEFT_SUPER || key == GLFW_KEY_RIGHT_SUPER) return GLFW_MOD_SUPER;
        return 0;
    }
}