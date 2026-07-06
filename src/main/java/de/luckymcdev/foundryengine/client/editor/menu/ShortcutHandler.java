package de.luckymcdev.foundryengine.client.editor.menu;

import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.imgui.ImGuiShortcut;

public class ShortcutHandler implements MenuSection {
	private final EditorManager editor;

	public ShortcutHandler(EditorManager editor) {
		this.editor = editor;
	}

	@Override
	public void render() {
	}

	public void handleShortcuts() {
		if (editor == null) {
			throw new IllegalStateException("EditorManager is null in ShortcutHandler");
		}
		for (Panel panel : editor.getPanels()) {
			handlePanelShortcut(panel);
		}
	}

	private void handlePanelShortcut(Panel panel) {
		ImGuiShortcut imGuiShortcut = panel.getShortcut();
		if (imGuiShortcut.isPressed()) {
			editor.togglePanel(panel);
		}
	}
}
