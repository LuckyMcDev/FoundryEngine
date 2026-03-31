package de.luckymcdev.foundryengine.client.editor.styles.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;

public class VidlibTheme implements ImTheme {

    private static final Color BG_MAIN = new Color(0.133f, 0.133f, 0.157f, 1.000f);
    private static final Color POPUP_BG = new Color(0.051f, 0.051f, 0.067f, 0.890f);
    private static final Color FRAME_BG = new Color(0.082f, 0.082f, 0.110f, 1.000f);
    private static final Color TITLE_BG = new Color(0.004f, 0.004f, 0.004f, 1.000f);
    private static final Color TITLE_COLLAPSED = new Color(0.318f, 0.498f, 0.439f, 0.937f);
    private static final Color BLUE_GRAB = new Color(0.267f, 0.400f, 0.573f, 1.000f);
    private static final Color BUTTON_BLUE = new Color(0.259f, 0.588f, 0.980f, 0.400f);

    @Override
    public String getName() {
        return "VidLib";
    }

    @Override
    public void applyTheme(ImGuiStyle s) {
        col(s, ImGuiCol.WindowBg, BG_MAIN);
        col(s, ImGuiCol.PopupBg, POPUP_BG);
        col(s, ImGuiCol.FrameBg, FRAME_BG);
        col(s, ImGuiCol.TitleBg, TITLE_BG);
        col(s, ImGuiCol.TitleBgActive, TITLE_BG);
        col(s, ImGuiCol.TitleBgCollapsed, TITLE_COLLAPSED);
        col(s, ImGuiCol.MenuBarBg, BG_MAIN);
        col(s, ImGuiCol.SliderGrab, BLUE_GRAB);
        col(s, ImGuiCol.Button, BUTTON_BLUE);
        col(s, ImGuiCol.ButtonHovered, BUTTON_BLUE);
        col(s, ImGuiCol.ButtonActive, BUTTON_BLUE);

        rounding(s, 4, 3, 2, 0, 3, 1, 3);
        borders(s, 0, 0, 0, 0, 0);
        padding(s, 4, 4);
        framePadding(s, 4, 1);
        itemSpacing(s, 6, 4);
        itemInnerSpacing(s, 8, 6);
        s.setIndentSpacing(25);
        s.setScrollbarSize(13);
        s.setGrabMinSize(16);
        s.setSelectableTextAlign(0, 0.5f);
        s.setWindowMenuButtonPosition(ImGuiDir.None);
    }
}