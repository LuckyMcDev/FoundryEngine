package de.luckymcdev.foundryengine.client.editor.menu.builtin;

import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import de.luckymcdev.foundryengine.client.editor.menu.PanelMenuItemRenderer;
import imgui.ImGui;

public class OpenMenuSection implements MenuSection {
    private final EditorManager editor;
    private final PanelMenuItemRenderer menuItemRenderer;

    public OpenMenuSection(EditorManager editor) {
        this.editor = editor;
        this.menuItemRenderer = new PanelMenuItemRenderer(editor);
    }

    @Override
    public void render() {
        if (ImGui.beginMenu("Open")) {
            editor.getPanels().stream()
                    .filter(p -> !(p instanceof EditorPanel))
                    .forEach(menuItemRenderer::render);
            ImGui.endMenu();
        }
    }
}