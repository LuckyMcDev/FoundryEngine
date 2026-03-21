package de.luckymcdev.foundryengine.client.editor.menu.builtin;

import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.builtin.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import de.luckymcdev.foundryengine.client.editor.menu.PanelMenuItemRenderer;
import imgui.ImGui;

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