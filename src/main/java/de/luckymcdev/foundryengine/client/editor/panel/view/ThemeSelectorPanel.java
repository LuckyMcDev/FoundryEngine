package de.luckymcdev.foundryengine.client.editor.panel.view;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.client.editor.styles.ImThemes;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
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
