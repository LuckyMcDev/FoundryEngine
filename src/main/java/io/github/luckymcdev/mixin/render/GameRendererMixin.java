package io.github.luckymcdev.mixin.render;

import imgui.ImGui;

import io.github.luckymcdev.client.editor.BuiltInEditor;
import io.github.luckymcdev.client.editor.panels.NodeEditorPanel;
import io.github.luckymcdev.client.editor.panels.TestPanel;
import io.github.luckymcdev.client.imgui.ImGuiHandler;

import io.github.luckymcdev.common.Instances;
import io.github.luckymcdev.interfaces.TbGameRenderer;
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

        if(ImGui.button("open test panel")) {
            tb$builtInEditor.openPanel(TestPanel.INSTANCE);
        }
        if(ImGui.button("close test panel")) {
            tb$builtInEditor.closePanel(TestPanel.INSTANCE);
        }
        ImGui.separator();
        if(ImGui.button("open node editor panel")) {
            tb$builtInEditor.openPanel(NodeEditorPanel.INSTANCE);
        }
        if(ImGui.button("close node editor panel")) {
            tb$builtInEditor.closePanel(NodeEditorPanel.INSTANCE);
        }

        ImGui.end();

        ImGuiHandler.endImGuiRendering();
    }


}