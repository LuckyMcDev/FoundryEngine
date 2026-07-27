package de.luckymcdev.foundryengine.client.editor.panel.view;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;

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
