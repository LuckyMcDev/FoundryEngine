package de.luckymcdev.foundryengine.client.editor.styles.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public class ModernDarkTheme implements ImTheme {

    private static final Color TEXT = new Color(0.92f, 0.92f, 0.92f, 1.00f);
    private static final Color TEXT_DISABLED = new Color(0.50f, 0.50f, 0.50f, 1.00f);
    private static final Color BG_MAIN = new Color(0.13f, 0.14f, 0.15f, 1.00f);
    private static final Color POPUP_BG = new Color(0.10f, 0.10f, 0.11f, 0.94f);
    private static final Color BORDER = new Color(0.43f, 0.43f, 0.50f, 0.50f);
    private static final Color FRAME = new Color(0.20f, 0.21f, 0.22f, 1.00f);
    private static final Color FRAME_HOVER = new Color(0.25f, 0.26f, 0.27f, 1.00f);
    private static final Color FRAME_ACTIVE = new Color(0.18f, 0.19f, 0.20f, 1.00f);
    private static final Color TITLE = new Color(0.15f, 0.15f, 0.16f, 1.00f);
    private static final Color MENU_BAR = new Color(0.20f, 0.20f, 0.21f, 1.00f);
    private static final Color SCROLL_GRAB = new Color(0.28f, 0.28f, 0.29f, 1.00f);
    private static final Color SCROLL_HOVER = new Color(0.33f, 0.34f, 0.35f, 1.00f);
    private static final Color SCROLL_ACTIVE = new Color(0.40f, 0.40f, 0.41f, 1.00f);
    private static final Color CHECKMARK = new Color(0.76f, 0.76f, 0.76f, 1.00f);
    private static final Color ACCENT = new Color(0.28f, 0.56f, 1.00f, 1.00f);
    private static final Color ACCENT_ACTIVE = new Color(0.37f, 0.61f, 1.00f, 1.00f);
    private static final Color BUTTON = new Color(0.20f, 0.25f, 0.30f, 1.00f);
    private static final Color BUTTON_HOVER = new Color(0.30f, 0.35f, 0.40f, 1.00f);
    private static final Color BUTTON_ACTIVE = new Color(0.25f, 0.30f, 0.35f, 1.00f);
    private static final Color HEADER = new Color(0.25f, 0.25f, 0.25f, 0.80f);
    private static final Color HEADER_HOVER = new Color(0.30f, 0.30f, 0.30f, 0.80f);
    private static final Color HEADER_ACTIVE = new Color(0.35f, 0.35f, 0.35f, 0.80f);
    private static final Color SEPARATOR_ACTIVE = new Color(0.33f, 0.67f, 1.00f, 1.00f);
    private static final Color TAB = new Color(0.15f, 0.18f, 0.22f, 1.00f);
    private static final Color TAB_HOVER = new Color(0.38f, 0.48f, 0.69f, 1.00f);
    private static final Color TAB_ACTIVE = new Color(0.28f, 0.38f, 0.59f, 1.00f);
    private static final Color PLOT_LINES = new Color(0.61f, 0.61f, 0.61f, 1.00f);
    private static final Color PLOT_HOVER = new Color(1.00f, 0.43f, 0.35f, 1.00f);
    private static final Color PLOT_HIST = new Color(0.90f, 0.70f, 0.00f, 1.00f);
    private static final Color PLOT_HIST_HOVER = new Color(1.00f, 0.60f, 0.00f, 1.00f);
    private static final Color TABLE_BG = new Color(0.19f, 0.19f, 0.20f, 1.00f);
    private static final Color TABLE_BORDER_STRONG = new Color(0.31f, 0.31f, 0.35f, 1.00f);
    private static final Color TABLE_BORDER_LIGHT = new Color(0.23f, 0.23f, 0.25f, 1.00f);
    private static final Color DIM_BG = new Color(0.80f, 0.80f, 0.80f, 0.20f);
    private static final Color MODAL_DIM = new Color(0.80f, 0.80f, 0.80f, 0.35f);

    @Override
    public String getName() {
        return "Modern Dark";
    }

    @Override
    public void applyTheme(ImGuiStyle s) {
        col(s, ImGuiCol.Text, TEXT);
        col(s, ImGuiCol.TextDisabled, TEXT_DISABLED);
        col(s, ImGuiCol.WindowBg, BG_MAIN);
        col(s, ImGuiCol.ChildBg, BG_MAIN);
        col(s, ImGuiCol.PopupBg, POPUP_BG);
        col(s, ImGuiCol.Border, BORDER);
        col(s, ImGuiCol.BorderShadow, 0, 0, 0, 0);

        col(s, ImGuiCol.FrameBg, FRAME);
        col(s, ImGuiCol.FrameBgHovered, FRAME_HOVER);
        col(s, ImGuiCol.FrameBgActive, FRAME_ACTIVE);

        col(s, ImGuiCol.TitleBg, TITLE);
        col(s, ImGuiCol.TitleBgActive, TITLE);
        col(s, ImGuiCol.TitleBgCollapsed, TITLE);
        col(s, ImGuiCol.MenuBarBg, MENU_BAR);

        col(s, ImGuiCol.ScrollbarBg, FRAME);
        col(s, ImGuiCol.ScrollbarGrab, SCROLL_GRAB);
        col(s, ImGuiCol.ScrollbarGrabHovered, SCROLL_HOVER);
        col(s, ImGuiCol.ScrollbarGrabActive, SCROLL_ACTIVE);

        col(s, ImGuiCol.CheckMark, CHECKMARK);
        col(s, ImGuiCol.SliderGrab, ACCENT);
        col(s, ImGuiCol.SliderGrabActive, ACCENT_ACTIVE);

        col(s, ImGuiCol.Button, BUTTON);
        col(s, ImGuiCol.ButtonHovered, BUTTON_HOVER);
        col(s, ImGuiCol.ButtonActive, BUTTON_ACTIVE);

        col(s, ImGuiCol.Header, HEADER);
        col(s, ImGuiCol.HeaderHovered, HEADER_HOVER);
        col(s, ImGuiCol.HeaderActive, HEADER_ACTIVE);

        col(s, ImGuiCol.Separator, BORDER);
        col(s, ImGuiCol.SeparatorHovered, SEPARATOR_ACTIVE);
        col(s, ImGuiCol.SeparatorActive, SEPARATOR_ACTIVE);

        col(s, ImGuiCol.ResizeGrip, ACCENT);
        col(s, ImGuiCol.ResizeGripHovered, ACCENT_ACTIVE);
        col(s, ImGuiCol.ResizeGripActive, ACCENT_ACTIVE);

        col(s, ImGuiCol.Tab, TAB);
        col(s, ImGuiCol.TabHovered, TAB_HOVER);
        col(s, ImGuiCol.TabActive, TAB_ACTIVE);
        col(s, ImGuiCol.TabUnfocused, TAB);
        col(s, ImGuiCol.TabUnfocusedActive, TAB);

        col(s, ImGuiCol.DockingPreview, ACCENT);
        col(s, ImGuiCol.DockingEmptyBg, BG_MAIN);

        col(s, ImGuiCol.PlotLines, PLOT_LINES);
        col(s, ImGuiCol.PlotLinesHovered, PLOT_HOVER);
        col(s, ImGuiCol.PlotHistogram, PLOT_HIST);
        col(s, ImGuiCol.PlotHistogramHovered, PLOT_HIST_HOVER);

        col(s, ImGuiCol.TableHeaderBg, TABLE_BG);
        col(s, ImGuiCol.TableBorderStrong, TABLE_BORDER_STRONG);
        col(s, ImGuiCol.TableBorderLight, TABLE_BORDER_LIGHT);
        col(s, ImGuiCol.TableRowBg, 0, 0, 0, 0);
        col(s, ImGuiCol.TableRowBgAlt, 1, 1, 1, 0.06f);

        col(s, ImGuiCol.TextSelectedBg, ACCENT.r(), ACCENT.g(), ACCENT.b(), 0.35f);
        col(s, ImGuiCol.DragDropTarget, ACCENT.r(), ACCENT.g(), ACCENT.b(), 0.90f);
        col(s, ImGuiCol.NavHighlight, ACCENT);
        col(s, ImGuiCol.NavWindowingHighlight, 1, 1, 1, 0.70f);
        col(s, ImGuiCol.NavWindowingDimBg, DIM_BG);
        col(s, ImGuiCol.ModalWindowDimBg, MODAL_DIM);

        rounding(s, 5.3f, 2.3f, 0, 0, 0, 0, 0);
        padding(s, 8, 8);
        framePadding(s, 5, 5);
        itemSpacing(s, 6, 6);
        itemInnerSpacing(s, 6, 6);
        s.setIndentSpacing(25);
        s.setWindowTitleAlign(0.5f, 0.5f);
    }
}