package de.luckymcdev.foundryengine.client.editor.menu.builtin;

import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import imgui.ImGui;
import imgui.type.ImBoolean;

public class ToolsMenuSection implements MenuSection {
    private final EditorManager editor;
    private final ImBoolean demoWindowOpen = new ImBoolean(false);
    private final ImBoolean metricsWindowOpen = new ImBoolean(false);

    public ToolsMenuSection(EditorManager editor) {
        this.editor = editor;
    }

    @Override
    public void render() {
        if (ImGui.beginMenu("Tools")) {
            renderCloseAllPanels();
            ImGui.separator();
            renderDebugOptions();
            ImGui.endMenu();
        }

        renderDebugWindows();
    }

    private void renderCloseAllPanels() {
        if (ImGui.menuItem("Close All Panels")) {
            editor.closeAllPanels();
        }
    }

    private void renderDebugOptions() {
        if (ImGui.menuItem("ImGui Demo", "", demoWindowOpen.get())) {
            demoWindowOpen.set(!demoWindowOpen.get());
        }

        if (ImGui.menuItem("ImGui Metrics", "", metricsWindowOpen.get())) {
            metricsWindowOpen.set(!metricsWindowOpen.get());
        }
    }

    private void renderDebugWindows() {
        if (demoWindowOpen.get()) {
            ImGui.showDemoWindow(demoWindowOpen);
        }
        if (metricsWindowOpen.get()) {
            ImGui.showMetricsWindow(metricsWindowOpen);
        }
    }
}