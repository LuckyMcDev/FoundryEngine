package de.luckymcdev.foundryengine.client.editor.styles.theme.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.theme.ImTheme;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public class ModernDarkTheme implements ImTheme {
    @Override
    public String getName() {
        return "ModernDark";
    }

    /**
     * Applies the ModernDark theme to the given ImGuiStyle.
     *
     * @param style The ImGuiStyle to apply the theme to.
     */
    @Override
    public void applyTheme(ImGuiStyle style) {
        // Base color scheme
        style.setColor(ImGuiCol.Text, 0.92f, 0.92f, 0.92f, 1.00f);
        style.setColor(ImGuiCol.TextDisabled, 0.50f, 0.50f, 0.50f, 1.00f);
        style.setColor(ImGuiCol.WindowBg, 0.13f, 0.14f, 0.15f, 1.00f);
        style.setColor(ImGuiCol.ChildBg, 0.13f, 0.14f, 0.15f, 1.00f);
        style.setColor(ImGuiCol.PopupBg, 0.10f, 0.10f, 0.11f, 0.94f);
        style.setColor(ImGuiCol.Border, 0.43f, 0.43f, 0.50f, 0.50f);
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.FrameBg, 0.20f, 0.21f, 0.22f, 1.00f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.25f, 0.26f, 0.27f, 1.00f);
        style.setColor(ImGuiCol.FrameBgActive, 0.18f, 0.19f, 0.20f, 1.00f);
        style.setColor(ImGuiCol.TitleBg, 0.15f, 0.15f, 0.16f, 1.00f);
        style.setColor(ImGuiCol.TitleBgActive, 0.15f, 0.15f, 0.16f, 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.15f, 0.15f, 0.16f, 1.00f);
        style.setColor(ImGuiCol.MenuBarBg, 0.20f, 0.20f, 0.21f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarBg, 0.20f, 0.21f, 0.22f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.28f, 0.28f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.33f, 0.34f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.40f, 0.40f, 0.41f, 1.00f);
        style.setColor(ImGuiCol.CheckMark, 0.76f, 0.76f, 0.76f, 1.00f);
        style.setColor(ImGuiCol.SliderGrab, 0.28f, 0.56f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.37f, 0.61f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.Button, 0.20f, 0.25f, 0.30f, 1.00f);
        style.setColor(ImGuiCol.ButtonHovered, 0.30f, 0.35f, 0.40f, 1.00f);
        style.setColor(ImGuiCol.ButtonActive, 0.25f, 0.30f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.Header, 0.25f, 0.25f, 0.25f, 0.80f);
        style.setColor(ImGuiCol.HeaderHovered, 0.30f, 0.30f, 0.30f, 0.80f);
        style.setColor(ImGuiCol.HeaderActive, 0.35f, 0.35f, 0.35f, 0.80f);
        style.setColor(ImGuiCol.Separator, 0.43f, 0.43f, 0.50f, 0.50f);
        style.setColor(ImGuiCol.SeparatorHovered, 0.33f, 0.67f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.SeparatorActive, 0.33f, 0.67f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.ResizeGrip, 0.28f, 0.56f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.ResizeGripHovered, 0.37f, 0.61f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.ResizeGripActive, 0.37f, 0.61f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.Tab, 0.15f, 0.18f, 0.22f, 1.00f);
        style.setColor(ImGuiCol.TabHovered, 0.38f, 0.48f, 0.69f, 1.00f);
        style.setColor(ImGuiCol.TabActive, 0.28f, 0.38f, 0.59f, 1.00f);
        style.setColor(ImGuiCol.TabUnfocused, 0.15f, 0.18f, 0.22f, 1.00f);
        style.setColor(ImGuiCol.TabUnfocusedActive, 0.15f, 0.18f, 0.22f, 1.00f);
        style.setColor(ImGuiCol.DockingPreview, 0.28f, 0.56f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.DockingEmptyBg, 0.13f, 0.14f, 0.15f, 1.00f);
        style.setColor(ImGuiCol.PlotLines, 0.61f, 0.61f, 0.61f, 1.00f);
        style.setColor(ImGuiCol.PlotLinesHovered, 1.00f, 0.43f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogram, 0.90f, 0.70f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogramHovered, 1.00f, 0.60f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.TableHeaderBg, 0.19f, 0.19f, 0.20f, 1.00f);
        style.setColor(ImGuiCol.TableBorderStrong, 0.31f, 0.31f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.TableBorderLight, 0.23f, 0.23f, 0.25f, 1.00f);
        style.setColor(ImGuiCol.TableRowBg, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.TableRowBgAlt, 1.00f, 1.00f, 1.00f, 0.06f);
        style.setColor(ImGuiCol.TextSelectedBg, 0.28f, 0.56f, 1.00f, 0.35f);
        style.setColor(ImGuiCol.DragDropTarget, 0.28f, 0.56f, 1.00f, 0.90f);
        style.setColor(ImGuiCol.NavHighlight, 0.28f, 0.56f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.NavWindowingHighlight, 1.00f, 1.00f, 1.00f, 0.70f);
        style.setColor(ImGuiCol.NavWindowingDimBg, 0.80f, 0.80f, 0.80f, 0.20f);
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.80f, 0.80f, 0.80f, 0.35f);

        // Style adjustments
        style.setWindowRounding(5.3f);
        style.setFrameRounding(2.3f);
        style.setScrollbarRounding(0);

        style.setWindowTitleAlign(0.50f, 0.50f);
        style.setWindowPadding(8.0f, 8.0f);
        style.setFramePadding(5.0f, 5.0f);
        style.setItemSpacing(6.0f, 6.0f);
        style.setItemInnerSpacing(6.0f, 6.0f);
        style.setIndentSpacing(25.0f);
    }
}
