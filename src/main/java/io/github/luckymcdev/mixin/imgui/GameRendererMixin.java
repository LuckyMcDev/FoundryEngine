package io.github.luckymcdev.mixin.imgui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImGuiIO;
import io.github.luckymcdev.client.imgui.ImGuiImpl;
import io.github.luckymcdev.client.imgui.icon.ImIcon;
import io.github.luckymcdev.client.imgui.icon.ImIcons;
import io.github.luckymcdev.client.imgui.node.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    private static NodeEditorInstance<String> nodeEditor;
    private static NodePinType<String> stringType;

    @Inject(method = "render", at = @At("HEAD"))
    private void renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {

    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        // Check window size changes BEFORE ImGui rendering
        var window = Minecraft.getInstance().getWindow();
        var prevWidth = window.getWidth();
        var prevHeight = window.getHeight();

        ImGuiImpl.beginImGuiRendering();

        ImGui.begin("Test");
        ImFont font = ImGui.getFont();

        ImGui.text("Loaded Font: "+ font + " size: "+ font.getFontSize());

        ImGui.text(ImIcons.FA.FA_FILE_TEXT + " " + ImIcons.DEV.DEV_NODEJS + ImIcons.SETI.SETI_APPLE);

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


        if (window.getWidth() != prevWidth || window.getHeight() != prevHeight) {
            Minecraft.getInstance().resizeDisplay();
        }


        ImGuiImpl.endImGuiRendering();
    }


}