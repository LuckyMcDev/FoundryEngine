package io.github.luckymcdev.foundryengine.mixin.render;

import imgui.ImGui;

import io.github.luckymcdev.foundryengine.client.editor.BuiltInEditor;
import io.github.luckymcdev.foundryengine.client.editor.panels.NodeEditorPanel;
import io.github.luckymcdev.foundryengine.client.editor.panels.PostProcessPanel;
import io.github.luckymcdev.foundryengine.client.editor.panels.TestPanel;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiHandler;

import io.github.luckymcdev.foundryengine.common.Instances;
import io.github.luckymcdev.foundryengine.interfaces.TbGameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements TbGameRenderer {

    @Unique
    private static final BuiltInEditor tb$builtInEditor = Instances.getBuiltInEditor();

    @Override
    @Inject(method = "render", at = @At("HEAD"))
    public void tb$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
    }

    @Override
    @Inject(method = "render", at = @At("RETURN"))
    public void tb$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ImGuiHandler.beginImGuiRendering();

        // Render the UI
        tb$builtInEditor.handleRender();

        ImGui.begin("editor manager");

        if(ImGui.button("toggle test panel")) {
            tb$builtInEditor.togglePanel(TestPanel.INSTANCE);
        }
        ImGui.separator();
        if(ImGui.button("toggle node editor panel")) {
            tb$builtInEditor.togglePanel(NodeEditorPanel.INSTANCE);
        }
        ImGui.separator();
        if(ImGui.button("toggle post processing panel")) {
            tb$builtInEditor.togglePanel(PostProcessPanel.INSTANCE);
        }

        ImGui.end();

        ImGuiHandler.endImGuiRendering();
    }


}