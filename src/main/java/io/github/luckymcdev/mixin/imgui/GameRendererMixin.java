package io.github.luckymcdev.mixin.imgui;

import imgui.ImGui;
import io.github.luckymcdev.imgui.ImGuiImpl;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ImGuiImpl.beginImGuiRendering();

        ImGui.showDebugLogWindow();

        ImGuiImpl.endImGuiRendering();
    }

}