package io.github.luckymcdev.foundryengine.client.editor;

import com.mojang.datafixers.kinds.IdF;
import imgui.ImGui;
import imgui.type.ImBoolean;
import io.github.luckymcdev.foundryengine.client.editor.panels.NodeEditorPanel;
import io.github.luckymcdev.foundryengine.client.editor.panels.PostProcessPanel;
import io.github.luckymcdev.foundryengine.client.editor.panels.TestPanel;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiHandler;
import io.github.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.minecraft.tags.InstrumentTags;

public class MainMenu {

    private static final BuiltInEditor editor = Instances.getBuiltInEditor();

    private static final ImBoolean demoWindowOpen = new ImBoolean(false);
    private static final ImBoolean metricsWindowOpen = new ImBoolean(false);

    public static void handleRender() {
        if (ImGui.beginMainMenuBar()) {
            ImGuiGraphicsStack gs = ImGuiHandler.getGraphicsStack();
            gs.push();

            renderPanelsMenu();
            renderToolsMenu();
            renderViewMenu();

            gs.pop();
            ImGui.endMainMenuBar();
        }
    }

    private static void renderPanelsMenu() {
        if (ImGui.beginMenu("Panels")) {
            if (ImGui.menuItem("Test Panel", "ctrl + t", isOpen(TestPanel.INSTANCE))) {
                editor.togglePanel(TestPanel.INSTANCE);
            }

            if (ImGui.menuItem("Node Editor", "ctrl + n", isOpen(NodeEditorPanel.INSTANCE))) {
                editor.togglePanel(NodeEditorPanel.INSTANCE);
            }

            if (ImGui.menuItem("Post Processing", "ctrl + p", isOpen(PostProcessPanel.INSTANCE))) {
                editor.togglePanel(PostProcessPanel.INSTANCE);
            }

            ImGui.endMenu();
        }
    }

    private static void renderToolsMenu() {
        if (ImGui.beginMenu("Tools")) {
            if (ImGui.menuItem("Close All Panels")) {
                closeAllPanels();
            }

            ImGui.separator();

            if (ImGui.menuItem("ImGui Demo", "", demoWindowOpen.get())) {
                demoWindowOpen.set(!demoWindowOpen.get());
            }

            if (ImGui.menuItem("ImGui Metrics", "", metricsWindowOpen.get())) {
                metricsWindowOpen.set(!metricsWindowOpen.get());
            }

            ImGui.endMenu();
        }

        if (demoWindowOpen.get()) {
            ImGui.showDemoWindow(demoWindowOpen);
        }

        if (metricsWindowOpen.get()) {
            ImGui.showMetricsWindow(metricsWindowOpen);
        }
    }

    private static void renderViewMenu() {
        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Reset Layout")) {
                // Add layout reset logic if you implement docking
            }

            ImGui.endMenu();
        }
    }

    private static boolean isOpen(Panel panel) {
        return editor.isOpen(panel);
    }

    private static void closeAllPanels() {
        editor.closeAllPanels();
    }
}