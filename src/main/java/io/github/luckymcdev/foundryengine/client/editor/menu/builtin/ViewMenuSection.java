package io.github.luckymcdev.foundryengine.client.editor.menu.builtin;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.editor.EditorManager;
import io.github.luckymcdev.foundryengine.client.editor.menu.MenuSection;

public class ViewMenuSection implements MenuSection {
    private final EditorManager editor;

    public ViewMenuSection(EditorManager editor) {
        this.editor = editor;
    }

    @Override
    public void render() {
        if (ImGui.beginMenu("View")) {
            renderResetLayout();
            ImGui.endMenu();
        }
    }

    private void renderResetLayout() {
        if (ImGui.menuItem("Reset Layout")) {
            // TODO: Implement docking layout reset
            resetLayout();
        }
    }

    /**
     * TODO: Implement layout reset functionality.
     */
    private void resetLayout() {
        // Future implementation for resetting the docking layout
    }
}