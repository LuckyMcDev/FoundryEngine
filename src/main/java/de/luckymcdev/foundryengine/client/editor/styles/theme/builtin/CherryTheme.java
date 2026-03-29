package de.luckymcdev.foundryengine.client.editor.styles.theme.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.theme.ImTheme;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public class CherryTheme implements ImTheme {
    @Override
    public String getName() {
        return "Cherry";
    }

    /**
     * Applies the Cherry theme to the given ImGuiStyle.
     *
     * @param style The ImGuiStyle to apply the theme to.
     */
    @Override
    public void applyTheme(ImGuiStyle style) {
        // Cherry colors, 3 intensities
        float[] cherryHi = {0.502f, 0.075f, 0.256f, 1.0f};
        float[] cherryMed = {0.455f, 0.198f, 0.301f, 1.0f};
        float[] cherryLow = {0.232f, 0.201f, 0.271f, 1.0f};
        float[] cherryBg = {0.200f, 0.220f, 0.270f, 1.0f};
        float[] cherryText = {0.860f, 0.930f, 0.890f, 1.0f};

        style.setColor(ImGuiCol.Text, cherryText[0], cherryText[1], cherryText[2], 0.78f);
        style.setColor(ImGuiCol.TextDisabled, cherryText[0], cherryText[1], cherryText[2], 0.28f);
        style.setColor(ImGuiCol.WindowBg, 0.13f, 0.14f, 0.17f, 1.00f);
        style.setColor(ImGuiCol.ChildBg, cherryBg[0], cherryBg[1], cherryBg[2], 0.58f);
        style.setColor(ImGuiCol.PopupBg, cherryBg[0], cherryBg[1], cherryBg[2], 0.9f);
        style.setColor(ImGuiCol.Border, 0.31f, 0.31f, 1.00f, 0.00f);
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.FrameBg, cherryBg[0], cherryBg[1], cherryBg[2], 1.00f);
        style.setColor(ImGuiCol.FrameBgHovered, cherryMed[0], cherryMed[1], cherryMed[2], 0.78f);
        style.setColor(ImGuiCol.FrameBgActive, cherryMed[0], cherryMed[1], cherryMed[2], 1.00f);
        style.setColor(ImGuiCol.TitleBg, cherryLow[0], cherryLow[1], cherryLow[2], 1.00f);
        style.setColor(ImGuiCol.TitleBgActive, cherryHi[0], cherryHi[1], cherryHi[2], 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed, cherryBg[0], cherryBg[1], cherryBg[2], 0.75f);
        style.setColor(ImGuiCol.MenuBarBg, cherryBg[0], cherryBg[1], cherryBg[2], 0.47f);
        style.setColor(ImGuiCol.ScrollbarBg, cherryBg[0], cherryBg[1], cherryBg[2], 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.09f, 0.15f, 0.16f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, cherryMed[0], cherryMed[1], cherryMed[2], 0.78f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, cherryMed[0], cherryMed[1], cherryMed[2], 1.00f);
        style.setColor(ImGuiCol.CheckMark, 0.71f, 0.22f, 0.27f, 1.00f);
        style.setColor(ImGuiCol.SliderGrab, 0.47f, 0.77f, 0.83f, 0.14f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.71f, 0.22f, 0.27f, 1.00f);
        style.setColor(ImGuiCol.Button, 0.47f, 0.77f, 0.83f, 0.14f);
        style.setColor(ImGuiCol.ButtonHovered, cherryMed[0], cherryMed[1], cherryMed[2], 0.86f);
        style.setColor(ImGuiCol.ButtonActive, cherryMed[0], cherryMed[1], cherryMed[2], 1.00f);
        style.setColor(ImGuiCol.Header, cherryMed[0], cherryMed[1], cherryMed[2], 0.76f);
        style.setColor(ImGuiCol.HeaderHovered, cherryMed[0], cherryMed[1], cherryMed[2], 0.86f);
        style.setColor(ImGuiCol.HeaderActive, cherryHi[0], cherryHi[1], cherryHi[2], 1.00f);
        style.setColor(ImGuiCol.Separator, 0.14f, 0.16f, 0.19f, 1.00f);
        style.setColor(ImGuiCol.SeparatorHovered, cherryMed[0], cherryMed[1], cherryMed[2], 0.78f);
        style.setColor(ImGuiCol.SeparatorActive, cherryMed[0], cherryMed[1], cherryMed[2], 1.00f);
        style.setColor(ImGuiCol.ResizeGrip, 0.47f, 0.77f, 0.83f, 0.04f);
        style.setColor(ImGuiCol.ResizeGripHovered, cherryMed[0], cherryMed[1], cherryMed[2], 0.78f);
        style.setColor(ImGuiCol.ResizeGripActive, cherryMed[0], cherryMed[1], cherryMed[2], 1.00f);
        style.setColor(ImGuiCol.Tab, cherryLow[0], cherryLow[1], cherryLow[2], 1.00f);
        style.setColor(ImGuiCol.TabHovered, cherryMed[0], cherryMed[1], cherryMed[2], 0.86f);
        style.setColor(ImGuiCol.TabActive, cherryHi[0], cherryHi[1], cherryHi[2], 1.00f);
        style.setColor(ImGuiCol.TabUnfocused, cherryBg[0], cherryBg[1], cherryBg[2], 0.97f);
        style.setColor(ImGuiCol.TabUnfocusedActive, cherryLow[0], cherryLow[1], cherryLow[2], 1.00f);
        style.setColor(ImGuiCol.PlotLines, cherryText[0], cherryText[1], cherryText[2], 0.63f);
        style.setColor(ImGuiCol.PlotLinesHovered, cherryMed[0], cherryMed[1], cherryMed[2], 1.00f);
        style.setColor(ImGuiCol.PlotHistogram, cherryText[0], cherryText[1], cherryText[2], 0.63f);
        style.setColor(ImGuiCol.PlotHistogramHovered, cherryMed[0], cherryMed[1], cherryMed[2], 1.00f);
        style.setColor(ImGuiCol.TextSelectedBg, cherryMed[0], cherryMed[1], cherryMed[2], 0.43f);
        style.setColor(ImGuiCol.ModalWindowDimBg, cherryBg[0], cherryBg[1], cherryBg[2], 0.73f);
        style.setColor(ImGuiCol.Border, 0.539f, 0.479f, 0.255f, 0.162f);

        // Style adjustments
        style.setWindowPadding(6.0f, 4.0f);
        style.setWindowRounding(0.0f);
        style.setFramePadding(7.0f, 2.0f);
        style.setFrameRounding(3.0f);
        style.setItemSpacing(7.0f, 1.0f);
        style.setItemInnerSpacing(1.0f, 1.0f);
        style.setIndentSpacing(6.0f);
        style.setScrollbarSize(12.0f);
        style.setScrollbarRounding(16.0f);
        style.setGrabMinSize(20.0f);
        style.setGrabRounding(2.0f);
        style.setWindowTitleAlign(0.50f, 0.50f);
        style.setFrameBorderSize(0.0f);
        style.setWindowBorderSize(1.0f);
    }
}