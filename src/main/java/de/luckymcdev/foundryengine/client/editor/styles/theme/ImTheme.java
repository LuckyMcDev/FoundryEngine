package de.luckymcdev.foundryengine.client.editor.styles.theme;

import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.ImGuiStyle;

public interface ImTheme {
    String getName();

    void applyTheme(ImGuiStyle style);

    default void apply(ImGuiStyle style) {
        ImGui.styleColorsDark();
        applyTheme(style);
    }

    default void col(ImGuiStyle style, int imGuiCol, Color color) {
        style.setColor(imGuiCol, color.r(), color.g(), color.b(), color.a());
    }

    default void col(ImGuiStyle style, int imGuiCol, float r, float g, float b) {
        style.setColor(imGuiCol, r, g, b, 1.0f);
    }

    default void col(ImGuiStyle style, int imGuiCol, float r, float g, float b, float a) {
        style.setColor(imGuiCol, r, g, b, a);
    }

    default void col(ImGuiStyle style, int imGuiCol, int argb) {
        col(style, imGuiCol, new Color(argb));
    }

    default void padding(ImGuiStyle style, float x, float y) {
        style.setWindowPadding(x, y);
    }

    default void framePadding(ImGuiStyle style, float x, float y) {
        style.setFramePadding(x, y);
    }

    default void itemSpacing(ImGuiStyle style, float x, float y) {
        style.setItemSpacing(x, y);
    }

    default void itemInnerSpacing(ImGuiStyle style, float x, float y) {
        style.setItemInnerSpacing(x, y);
    }

    default void rounding(ImGuiStyle style, float window, float frame, float grab, float tab, float popup, float scrollbar, float child) {
        style.setWindowRounding(window);
        style.setFrameRounding(frame);
        style.setGrabRounding(grab);
        style.setTabRounding(tab);
        style.setPopupRounding(popup);
        style.setScrollbarRounding(scrollbar);
        style.setChildRounding(child);
    }

    default void borders(ImGuiStyle style, float window, float frame, float popup, float child, float tab) {
        style.setWindowBorderSize(window);
        style.setFrameBorderSize(frame);
        style.setPopupBorderSize(popup);
        style.setChildBorderSize(child);
        style.setTabBorderSize(tab);
    }
}