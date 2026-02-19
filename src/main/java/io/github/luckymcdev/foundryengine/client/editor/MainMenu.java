package io.github.luckymcdev.foundryengine.client.editor;

import imgui.ImGui;
import imgui.type.ImBoolean;
import io.github.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;
import io.github.luckymcdev.foundryengine.common.Instances;

public class MainMenu {

    private static final BuiltInEditor editor = Instances.getBuiltInEditor();

    private static final ImBoolean demoWindowOpen = new ImBoolean(false);
    private static final ImBoolean metricsWindowOpen = new ImBoolean(false);

    public static void handleRender() {
        if (ImGui.beginMainMenuBar()) {
            ImGuiGraphicsStack gs = Instances.getImGuiManager().getGraphicsStack();
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
            editor.getPanels().forEach(panel -> {
                if (ImGui.menuItem(panel.getLabel(), "", editor.isOpen(panel))) {
                    editor.togglePanel(panel);
                }
            });

            ImGui.endMenu();
        }
    }

    private static void renderToolsMenu() {
        if (ImGui.beginMenu("Tools")) {
            if (ImGui.menuItem("Close All Panels")) {
                editor.closeAllPanels();
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

        if (demoWindowOpen.get()) ImGui.showDemoWindow(demoWindowOpen);
        if (metricsWindowOpen.get()) ImGui.showMetricsWindow(metricsWindowOpen);
    }

    private static void renderViewMenu() {
        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Reset Layout")) {
                // TODO: docking layout reset
            }
            ImGui.endMenu();
        }
    }
}