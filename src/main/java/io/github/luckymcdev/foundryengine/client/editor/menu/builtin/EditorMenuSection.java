package io.github.luckymcdev.foundryengine.client.editor.menu.builtin;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.editor.EditorManager;
import io.github.luckymcdev.foundryengine.client.editor.builtin.editor.EditorPanel;
import io.github.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import io.github.luckymcdev.foundryengine.client.editor.menu.PanelMenuItemRenderer;

public class EditorMenuSection implements MenuSection {
    private final EditorManager editor;
    private final PanelMenuItemRenderer menuItemRenderer;

    public EditorMenuSection(EditorManager editor) {
        this.editor = editor;
        this.menuItemRenderer = new PanelMenuItemRenderer(editor);
    }

    @Override
    public void render() {
        if (ImGui.beginMenu("Editor")) {
            editor.getPanels().stream()
                    .filter(EditorPanel.class::isInstance)
                    .forEach(menuItemRenderer::render);
            ImGui.endMenu();
        }
    }
}