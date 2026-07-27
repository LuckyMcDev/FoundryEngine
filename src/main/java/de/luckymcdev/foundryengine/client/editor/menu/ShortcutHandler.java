package de.luckymcdev.foundryengine.client.editor.menu;

import de.luckymcdev.foundryengine.client.editor.EditorManager;

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
	}
}
