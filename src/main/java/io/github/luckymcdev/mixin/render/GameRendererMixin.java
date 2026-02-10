package io.github.luckymcdev.mixin.render;

import imgui.ImGui;

import io.github.luckymcdev.client.ClientMatrices;
import io.github.luckymcdev.client.editor.BuiltInEditor;
import io.github.luckymcdev.client.editor.panels.NodeEditorPanel;
import io.github.luckymcdev.client.editor.panels.TestPanel;
import io.github.luckymcdev.client.imgui.ImGuiHandler;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    private static final BuiltInEditor EDITOR = new BuiltInEditor();

    @Inject(method = "render", at = @At("HEAD"))
    private void renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {

    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ImGuiHandler.beginImGuiRendering();

        // Render the UI
        EDITOR.handle();

        ImGui.begin("editor manager");

        if(ImGui.button("open test panel")) {
            EDITOR.openPanel(TestPanel.INSTANCE);
        }
        if(ImGui.button("close test panel")) {
            EDITOR.closePanel(TestPanel.INSTANCE);
        }
        ImGui.separator();
        if(ImGui.button("open node editor panel")) {
            EDITOR.openPanel(NodeEditorPanel.INSTANCE);
        }
        if(ImGui.button("close node editor panel")) {
            EDITOR.closePanel(NodeEditorPanel.INSTANCE);
        }

        ImGui.end();

        ImGuiHandler.endImGuiRendering();
    }


}