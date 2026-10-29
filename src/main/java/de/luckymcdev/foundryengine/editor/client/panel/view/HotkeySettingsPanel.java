package de.luckymcdev.foundryengine.editor.client.panel.view;

import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.editor.client.config.PanelCategory;
import de.luckymcdev.foundryengine.editor.client.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.imgui.client.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.imgui.client.icon.ImIcons;
import de.luckymcdev.foundryengine.Common;

public class HotkeySettingsPanel extends EditorPanel {
	public static final HotkeySettingsPanel INSTANCE = new HotkeySettingsPanel();

	public HotkeySettingsPanel() {
		super(new Builder(Common.id("hotkey_settings"))
			.icon(ImIcons.KEY)
			.category(PanelCategory.VIEW));
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		Client.getHotKeyManager().getImHotKey().render(
			Client.getHotKeyManager().getHotkeys()
		);
	}
}
