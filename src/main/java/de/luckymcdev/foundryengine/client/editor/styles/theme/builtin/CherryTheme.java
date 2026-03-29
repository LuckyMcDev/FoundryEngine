package de.luckymcdev.foundryengine.client.editor.styles.theme.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.theme.ImTheme;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public class CherryTheme implements ImTheme {

    private static final Color HI = new Color(0.502f, 0.075f, 0.256f, 1.0f);
    private static final Color MED = new Color(0.455f, 0.198f, 0.301f, 1.0f);
    private static final Color LOW = new Color(0.232f, 0.201f, 0.271f, 1.0f);
    private static final Color BG = new Color(0.200f, 0.220f, 0.270f, 1.0f);
    private static final Color TEXT = new Color(0.860f, 0.930f, 0.890f, 1.0f);
    private static final Color BLACK = new Color(0, 0, 0, 1);
    private static final Color WINDOW_BG = new Color(0.13f, 0.14f, 0.17f, 1.00f);
    private static final Color BORDER = new Color(0.539f, 0.479f, 0.255f, 0.162f);
    private static final Color SCROLL_GRAB = new Color(0.09f, 0.15f, 0.16f, 1.00f);
    private static final Color ACCENT_RED = new Color(0.71f, 0.22f, 0.27f, 1.00f);
    private static final Color BUTTON_BLUE_LOW = new Color(0.47f, 0.77f, 0.83f, 0.14f);
    private static final Color SEPARATOR = new Color(0.14f, 0.16f, 0.19f, 1.00f);
    private static final Color RESIZE_GRIP_LOW = new Color(0.47f, 0.77f, 0.83f, 0.04f);

    @Override
    public String getName() {
        return "Cherry";
    }

    @Override
    public void applyTheme(ImGuiStyle s) {
        col(s, ImGuiCol.Text, TEXT.lerp(0.22f, BLACK));
        col(s, ImGuiCol.TextDisabled, TEXT.lerp(0.72f, BLACK));
        col(s, ImGuiCol.WindowBg, WINDOW_BG);
        col(s, ImGuiCol.ChildBg, BG.r(), BG.g(), BG.b(), 0.58f);
        col(s, ImGuiCol.PopupBg, BG.r(), BG.g(), BG.b(), 0.90f);
        col(s, ImGuiCol.Border, BORDER);
        col(s, ImGuiCol.BorderShadow, 0, 0, 0, 0);

        col(s, ImGuiCol.FrameBg, BG);
        col(s, ImGuiCol.FrameBgHovered, MED.r(), MED.g(), MED.b(), 0.78f);
        col(s, ImGuiCol.FrameBgActive, MED);

        col(s, ImGuiCol.TitleBg, LOW);
        col(s, ImGuiCol.TitleBgActive, HI);
        col(s, ImGuiCol.TitleBgCollapsed, BG.r(), BG.g(), BG.b(), 0.75f);
        col(s, ImGuiCol.MenuBarBg, BG.r(), BG.g(), BG.b(), 0.47f);

        col(s, ImGuiCol.ScrollbarBg, BG);
        col(s, ImGuiCol.ScrollbarGrab, SCROLL_GRAB);
        col(s, ImGuiCol.ScrollbarGrabHovered, MED.r(), MED.g(), MED.b(), 0.78f);
        col(s, ImGuiCol.ScrollbarGrabActive, MED);

        col(s, ImGuiCol.CheckMark, ACCENT_RED);
        col(s, ImGuiCol.SliderGrab, BUTTON_BLUE_LOW);
        col(s, ImGuiCol.SliderGrabActive, ACCENT_RED);

        col(s, ImGuiCol.Button, BUTTON_BLUE_LOW);
        col(s, ImGuiCol.ButtonHovered, MED.r(), MED.g(), MED.b(), 0.86f);
        col(s, ImGuiCol.ButtonActive, MED);

        col(s, ImGuiCol.Header, MED.r(), MED.g(), MED.b(), 0.76f);
        col(s, ImGuiCol.HeaderHovered, MED.r(), MED.g(), MED.b(), 0.86f);
        col(s, ImGuiCol.HeaderActive, HI);

        col(s, ImGuiCol.Separator, SEPARATOR);
        col(s, ImGuiCol.SeparatorHovered, MED.r(), MED.g(), MED.b(), 0.78f);
        col(s, ImGuiCol.SeparatorActive, MED);

        col(s, ImGuiCol.ResizeGrip, RESIZE_GRIP_LOW);
        col(s, ImGuiCol.ResizeGripHovered, MED.r(), MED.g(), MED.b(), 0.78f);
        col(s, ImGuiCol.ResizeGripActive, MED);

        col(s, ImGuiCol.Tab, LOW);
        col(s, ImGuiCol.TabHovered, MED.r(), MED.g(), MED.b(), 0.86f);
        col(s, ImGuiCol.TabActive, HI);
        col(s, ImGuiCol.TabUnfocused, BG.r(), BG.g(), BG.b(), 0.97f);
        col(s, ImGuiCol.TabUnfocusedActive, LOW);

        col(s, ImGuiCol.PlotLines, TEXT.r(), TEXT.g(), TEXT.b(), 0.63f);
        col(s, ImGuiCol.PlotLinesHovered, MED);
        col(s, ImGuiCol.PlotHistogram, TEXT.r(), TEXT.g(), TEXT.b(), 0.63f);
        col(s, ImGuiCol.PlotHistogramHovered, MED);

        col(s, ImGuiCol.TextSelectedBg, MED.r(), MED.g(), MED.b(), 0.43f);
        col(s, ImGuiCol.ModalWindowDimBg, BG.r(), BG.g(), BG.b(), 0.73f);

        rounding(s, 0, 3, 2, 0, 0, 16, 0);
        borders(s, 1, 0, 0, 0, 0);
        padding(s, 6, 4);
        framePadding(s, 7, 2);
        itemSpacing(s, 7, 1);
        itemInnerSpacing(s, 1, 1);
        s.setIndentSpacing(6);
        s.setScrollbarSize(12);
        s.setGrabMinSize(20);
        s.setWindowTitleAlign(0.5f, 0.5f);
    }
}