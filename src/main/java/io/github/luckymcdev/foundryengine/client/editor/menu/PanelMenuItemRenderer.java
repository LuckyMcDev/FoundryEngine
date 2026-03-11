package io.github.luckymcdev.foundryengine.client.editor.menu;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.editor.EditorManager;
import io.github.luckymcdev.foundryengine.client.editor.Panel;

public class PanelMenuItemRenderer {
    private final EditorManager editor;

    public PanelMenuItemRenderer(EditorManager editor) {
        this.editor = editor;
    }

    public void render(Panel panel) {
        String shortcutLabel = getShortcutLabel(panel);
        boolean isOpen = editor.isOpen(panel);

        if (ImGui.menuItem(panel.getLabel(), shortcutLabel, isOpen)) {
            editor.togglePanel(panel);
        }
    }

    private String getShortcutLabel(Panel panel) {
        return panel.getShortcut() != null
                ? panel.getShortcut().toLabel()
                : "";
    }
}