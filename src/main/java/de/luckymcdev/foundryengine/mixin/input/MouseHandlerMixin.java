package de.luckymcdev.foundryengine.mixin.input;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.cutscene.ClientCutsceneManager;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneEditor;
import de.luckymcdev.foundryengine.client.cutscene.CutsceneRenderer;
import de.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import de.luckymcdev.foundryengine.interfaces.EngineMouseHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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
    public void engine$onButton(long handle, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptMouse()) {
            ci.cancel();
            return;
        }
        if (Minecraft.getInstance().screen == null
                && de.luckymcdev.foundryengine.client.cutscene.ClientCutsceneManager.shouldBlockInput()) {
            Minecraft mc = Minecraft.getInstance();
            mc.options.keyAttack.setDown(false);
            mc.options.keyUse.setDown(false);
            mc.options.keyPickItem.setDown(false);
            KeyMapping.releaseAll();
            ci.cancel();
        }
    }

    @Override
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    public void engine$onScroll(long handle, double horizontal, double vertical, CallbackInfo ci) {
        if (Client.getImGuiManager().shouldInterceptMouse()) {
            ci.cancel();
            return;
        }

        if (CutsceneRenderer.storedPoint != null) {
            CutsceneEditor.updateStoredDistance(vertical);
            ci.cancel();
            return;
        }

        if (Minecraft.getInstance().screen == null && ClientCutsceneManager.shouldBlockInput()) {
            ci.cancel();
        }
    }
}

