package de.luckymcdev.foundryengine.editor.client.panel.view;

import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.editor.client.config.PanelCategory;
import de.luckymcdev.foundryengine.editor.client.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.editor.client.style.ImTheme;
import de.luckymcdev.foundryengine.editor.client.style.ImThemes;
import de.luckymcdev.foundryengine.imgui.client.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.imgui.client.icon.ImIcons;
import de.luckymcdev.foundryengine.Common;
import imgui.ImGui;

public class ThemeSelectorPanel extends EditorPanel {
	public static final ThemeSelectorPanel INSTANCE = new ThemeSelectorPanel();

	public ThemeSelectorPanel() {
		super(new Builder(Common.id("theme_selector"))
			.icon(ImIcons.THEMECO)
			.category(PanelCategory.VIEW));
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		var manager = Client.getImGuiManager();
		ImTheme currentTheme = manager.getCurrentTheme();

		g.section("Available Themes");

		g.scrollableRegion("##theme_list", 0, ImGui.getContentRegionAvailY(), true, () -> {
			for (ImTheme theme : ImThemes.ALL) {
				boolean isSelected = currentTheme.getClass() == theme.getClass();

				if (ImGui.selectable(theme.getName(), isSelected)) {
					manager.setTheme(theme);
				}

				if (isSelected) {
					ImGui.setItemDefaultFocus();
				}
			}
		});
	}
}
