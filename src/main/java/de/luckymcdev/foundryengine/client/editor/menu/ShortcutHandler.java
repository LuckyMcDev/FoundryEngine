package de.luckymcdev.foundryengine.client.editor.menu;

import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;

public class ShortcutHandler implements MenuSection {
    private final EditorManager editor;

    public ShortcutHandler(EditorManager editor) {
        this.editor = editor;
    }

    @Override
    public void render() {
        // Empty as It's only for handling shortcuts and doesn't need to render anything.
    }

    public void handleShortcuts() {
        if (editor == null) {
            throw new IllegalStateException("EditorManager is null in ShortcutHandler");
        }
        editor.getPanels().forEach(this::handlePanelShortcut);
    }

    private void handlePanelShortcut(Panel panel) {
        Shortcut shortcut = panel.getShortcut();
        if (shortcut != null && shortcut.isPressed()) {
            editor.togglePanel(panel);
        }
    }
}