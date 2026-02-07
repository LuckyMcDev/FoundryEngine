package io.github.luckymcdev.mixin.imgui;

import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImGuiIO;
import io.github.luckymcdev.client.imgui.ImGuiImpl;
import io.github.luckymcdev.client.imgui.node.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    // Create the node editor instance (only once)
    private static NodeEditorInstance<String> nodeEditor;
    private static NodePinType<String> stringType;

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ImGuiImpl.beginImGuiRendering();

        ImGui.begin("Test");
        ImFont font = ImGui.getFont();

        ImGui.text("Loaded Font: "+ font + " size: "+ font.getFontSize());

        ImGui.text("nf-fa-camera  \uF030  \\uf030");

        ImGui.end();

        // Initialize node editor if not created
        if (nodeEditor == null) {
            stringType = new NodePinType<>("String", NodePinShape.CIRCLE, null);
            nodeEditor = new NodeEditorInstance<>(stringType);
        }

        // Render node editor window
        if (ImGui.begin("Node Editor")) {
            nodeEditor.render(node -> {
                // Context menu for creating nodes
                if (ImGui.menuItem("Text Node")) {
                    Node textNode = new Node("Text", List.of(
                            stringType.output("Text")
                    ));
                    nodeEditor.addNode(textNode);
                }

                if (ImGui.menuItem("Combine Node")) {
                    Node combineNode = new Node("Combine", List.of(
                            stringType.required("A"),
                            stringType.required("B"),
                            stringType.output("Result")
                    ));
                    nodeEditor.addNode(combineNode);
                }

                if (ImGui.menuItem("Clear All")) {
                    nodeEditor.clear();
                }
            });
        }
        ImGui.end();

        ImGuiImpl.endImGuiRendering();
    }

}