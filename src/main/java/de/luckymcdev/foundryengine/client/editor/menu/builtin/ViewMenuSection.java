package de.luckymcdev.foundryengine.client.editor.menu.builtin;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import de.luckymcdev.foundryengine.client.editor.styles.theme.ImTheme;
import de.luckymcdev.foundryengine.client.editor.styles.theme.ImThemes;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

public class ViewMenuSection implements MenuSection {
    public static final List<ImTheme> THEMES = new ArrayList<>();

    static {
        THEMES.add(ImThemes.BESS_DARK_IM_THEME);
        THEMES.add(ImThemes.CATPUCCIN_MOCHA_IM_THEME);
        THEMES.add(ImThemes.MODERN_DARK_IM_THEME);
        THEMES.add(ImThemes.DARK_IM_THEME);
        THEMES.add(ImThemes.CHERRY_IM_THEME);
        THEMES.add(ImThemes.VIDLIB_IM_THEME);
        THEMES.add(ImThemes.VEIL_IM_THEME);
    }

    @Override
    public void render() {
        if (ImGui.beginMenu("View")) {
            renderResetDocking();
            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Theme")) {
            renderThemeMenu();
            ImGui.endMenu();
        }
    }

    private void renderResetDocking() {
        if (ImGui.menuItem("Reset Docking")) {
            resetDocking();
        }
    }

    private void renderThemeMenu() {
        for (ImTheme theme : THEMES) {
            if (ImGui.menuItem(theme.getName())) {
                Client.getImGuiManager().setTheme(theme);
            }
        }
    }

    private void resetDocking() {
        int dockspaceId = Client.getImGuiManager().getDockId();
        imgui.internal.ImGui.dockBuilderRemoveNodeChildNodes(dockspaceId);
        imgui.internal.ImGui.dockBuilderRemoveNodeDockedWindows(dockspaceId);
    }
}