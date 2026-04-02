package de.luckymcdev.foundryengine.client.editor.menu;

import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.Panel;
import imgui.ImGui;

public class PanelMenuItemRenderer {
    private final EditorManager editor;

    public PanelMenuItemRenderer(EditorManager editor) {
        this.editor = editor;
    }

    public void render(Panel panel) {
        String shortcutLabel = getShortcutLabel(panel);
        boolean isOpen = editor.isOpen(panel);

        if (ImGui.menuItem(panel.getFormattedLabel(), shortcutLabel, isOpen)) {
            editor.togglePanel(panel);
        }
    }

    private String getShortcutLabel(Panel panel) {
        return panel.getShortcut() != null
                ? panel.getShortcut().toLabel()
                : "";
    }
}