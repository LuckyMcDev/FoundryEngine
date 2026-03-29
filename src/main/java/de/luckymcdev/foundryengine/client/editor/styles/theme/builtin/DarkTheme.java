package de.luckymcdev.foundryengine.client.editor.styles.theme.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.theme.ImTheme;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public class DarkTheme implements ImTheme {

    private static final Color BG_WINDOW = new Color(0.1000f, 0.1050f, 0.1100f, 1.00f);
    private static final Color BG_MED = new Color(0.2000f, 0.2050f, 0.2100f, 1.00f);
    private static final Color BG_HI = new Color(0.3000f, 0.3050f, 0.3100f, 1.00f);
    private static final Color BG_ACTIVE = new Color(0.1500f, 0.1505f, 0.1510f, 1.00f);
    private static final Color TAB_HOVER = new Color(0.3800f, 0.3805f, 0.3810f, 1.00f);
    private static final Color TAB_ACTIVE = new Color(0.2800f, 0.2805f, 0.2810f, 1.00f);

    @Override
    public String getName() {
        return "Dark";
    }

    @Override
    public void applyTheme(ImGuiStyle s) {
        col(s, ImGuiCol.WindowBg, BG_WINDOW);

        col(s, ImGuiCol.Header, BG_MED);
        col(s, ImGuiCol.HeaderHovered, BG_HI);
        col(s, ImGuiCol.HeaderActive, BG_ACTIVE);

        col(s, ImGuiCol.Button, BG_MED);
        col(s, ImGuiCol.ButtonHovered, BG_HI);
        col(s, ImGuiCol.ButtonActive, BG_ACTIVE);

        col(s, ImGuiCol.FrameBg, BG_MED);
        col(s, ImGuiCol.FrameBgHovered, BG_HI);
        col(s, ImGuiCol.FrameBgActive, BG_ACTIVE);

        col(s, ImGuiCol.Tab, BG_ACTIVE);
        col(s, ImGuiCol.TabHovered, TAB_HOVER);
        col(s, ImGuiCol.TabActive, TAB_ACTIVE);
        col(s, ImGuiCol.TabUnfocused, BG_ACTIVE);
        col(s, ImGuiCol.TabUnfocusedActive, BG_MED);

        col(s, ImGuiCol.TitleBg, BG_ACTIVE);
        col(s, ImGuiCol.TitleBgActive, BG_ACTIVE);
        col(s, ImGuiCol.TitleBgCollapsed, BG_ACTIVE);
    }
}