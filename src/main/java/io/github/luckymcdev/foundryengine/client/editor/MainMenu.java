package io.github.luckymcdev.foundryengine.client.editor;

import imgui.ImGui;
import imgui.type.ImBoolean;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;

/**
 * The Main Menu implementation. Manages the top info bar and also all Panels in the Panel Menu
 */
public class MainMenu {
    private static final EditorManager editor = Client.getEditorManager();
    private static final ImBoolean demoWindowOpen = new ImBoolean(false);
    private static final ImBoolean metricsWindowOpen = new ImBoolean(false);

    /**
     * Handles Rendering the Main Menu Bar.
     * {@link ImGui#beginMainMenuBar()}
     */
    public static void handleRender() {
        if (ImGui.beginMainMenuBar()) {
            ImGuiGraphicsStack gs = Client.getImGuiManager().getGraphicsStack();
            gs.push();

            renderPanelsMenu();
            renderToolsMenu();
            renderViewMenu();

            gs.pop();
            ImGui.endMainMenuBar();
        }
    }

    /**
     * Renders the Panels menu in the Main Menu Bar.
     */
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

    /**
     * Renders the tools Menu in the Main Menu Bar.
     */
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

    /**
     * TODO: make Impl work.
     * Renders the View Menu in the Main Menu Bar.
     */
    private static void renderViewMenu() {
        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Reset Layout")) {
                // TODO: docking layout reset
            }
            ImGui.endMenu();
        }
    }
}