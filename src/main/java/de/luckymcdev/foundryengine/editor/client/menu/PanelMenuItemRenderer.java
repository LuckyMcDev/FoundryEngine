package de.luckymcdev.foundryengine.editor.client.menu;

import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.editor.client.EditorManager;
import de.luckymcdev.foundryengine.editor.client.panel.Panel;
import de.luckymcdev.foundryengine.imgui.client.hotkey.ImHotKey;
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
		ImHotKey.HotKey hk = panel.getShortcut();
		return hk != null ? Client.getHotKeyManager().getImHotKey().getHotKeyLabel(hk.functionKeys, null) : "";
	}
}