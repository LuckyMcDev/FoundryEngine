package de.luckymcdev.foundryengine.client.editor.styles.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;

public class VeilTheme implements ImTheme {

    private static final Color TEXT = new Color(0.98f, 0.98f, 0.98f, 1.00f);
    private static final Color TEXT_DISABLED = new Color(0.55f, 0.55f, 0.56f, 1.00f);
    private static final Color BG_WINDOW = new Color(0.15f, 0.15f, 0.15f, 0.92f);
    private static final Color BG_POPUP = new Color(0.08f, 0.08f, 0.08f, 0.94f);
    private static final Color BORDER = new Color(0.35f, 0.35f, 0.35f, 0.50f);
    private static final Color BORDER_SHADOW = new Color(0.12f, 0.12f, 0.13f, 0.27f);
    private static final Color FRAME = new Color(0.12f, 0.13f, 0.13f, 0.94f);
    private static final Color FRAME_HOVER = new Color(0.15f, 0.15f, 0.15f, 0.94f);
    private static final Color FRAME_ACTIVE = new Color(0.31f, 0.31f, 0.31f, 0.67f);
    private static final Color TITLE = new Color(0.14f, 0.14f, 0.15f, 1.00f);
    private static final Color TITLE_COLLAPSED = new Color(0.00f, 0.00f, 0.00f, 0.51f);
    private static final Color MENU_BAR = new Color(0.28f, 0.29f, 0.29f, 0.53f);
    private static final Color SCROLL_BG = new Color(0.02f, 0.02f, 0.02f, 0.53f);
    private static final Color GRAY_MED = new Color(0.31f, 0.31f, 0.31f, 1.00f);
    private static final Color GRAY_LIGHT = new Color(0.41f, 0.41f, 0.41f, 1.00f);
    private static final Color GRAY_HI = new Color(0.51f, 0.51f, 0.51f, 1.00f);
    private static final Color ACCENT_GREEN = new Color(0.31f, 0.51f, 0.21f, 1.00f);
    private static final Color SLIDER_GRAB = new Color(0.40f, 0.40f, 0.40f, 0.53f);
    private static final Color BUTTON = new Color(0.28f, 0.29f, 0.29f, 1.00f);
    private static final Color BUTTON_ACTIVE = new Color(0.27f, 0.27f, 0.27f, 1.00f);
    private static final Color HEADER = new Color(0.73f, 0.73f, 0.73f, 0.31f);
    private static final Color HEADER_SELECTION = new Color(0.41f, 0.41f, 0.41f, 0.45f);
    private static final Color SEPARATOR = new Color(0.35f, 0.36f, 0.36f, 1.00f);
    private static final Color SEPARATOR_HOVER = new Color(0.10f, 0.40f, 0.75f, 0.78f);
    private static final Color SEPARATOR_ACTIVE = new Color(0.10f, 0.40f, 0.75f, 1.00f);
    private static final Color RESIZE_GRIP = new Color(0.11f, 0.11f, 0.11f, 1.00f);
    private static final Color RESIZE_GRIP_HOVER = new Color(0.54f, 0.54f, 0.54f, 1.00f);
    private static final Color RESIZE_GRIP_ACTIVE = new Color(0.85f, 0.85f, 0.85f, 1.00f);
    private static final Color TAB = new Color(0.32f, 0.32f, 0.32f, 0.53f);
    private static final Color TAB_HOVER = new Color(0.35f, 0.35f, 0.35f, 0.80f);
    private static final Color TAB_ACTIVE = new Color(0.39f, 0.39f, 0.39f, 1.00f);
    private static final Color TAB_UNFOCUSED = new Color(0.07f, 0.10f, 0.15f, 0.97f);
    private static final Color TAB_UNFOCUSED_ACTIVE = new Color(0.14f, 0.26f, 0.42f, 1.00f);
    private static final Color DOCKING_PREVIEW = new Color(0.24f, 0.31f, 0.41f, 0.53f);
    private static final Color DOCKING_BG = new Color(0.20f, 0.20f, 0.20f, 1.00f);
    private static final Color PLOT_LINES = new Color(0.61f, 0.61f, 0.61f, 1.00f);
    private static final Color PLOT_HOVER = new Color(1.00f, 0.43f, 0.35f, 1.00f);
    private static final Color PLOT_HIST = new Color(0.90f, 0.70f, 0.00f, 1.00f);
    private static final Color PLOT_HIST_HOVER = new Color(1.00f, 0.60f, 0.00f, 1.00f);
    private static final Color TABLE_BG = new Color(0.19f, 0.19f, 0.20f, 1.00f);
    private static final Color TABLE_BORDER_STRONG = new Color(0.31f, 0.31f, 0.35f, 1.00f);
    private static final Color TABLE_BORDER_LIGHT = new Color(0.23f, 0.23f, 0.25f, 1.00f);
    private static final Color SELECTION_BLUE = new Color(0.26f, 0.59f, 0.98f, 0.35f);
    private static final Color DRAG_DROP = new Color(0.35f, 0.28f, 0.20f, 1.00f);
    private static final Color NAV_HIGHLIGHT = new Color(0.76f, 0.76f, 0.76f, 1.00f);
    private static final Color DIM_BG = new Color(0.80f, 0.80f, 0.80f, 0.20f);
    private static final Color MODAL_DIM = new Color(0.80f, 0.80f, 0.80f, 0.35f);

    @Override
    public String getName() {
        return "Veil";
    }

    @Override
    public void applyTheme(ImGuiStyle s) {
        col(s, ImGuiCol.Text, TEXT);
        col(s, ImGuiCol.TextDisabled, TEXT_DISABLED);
        col(s, ImGuiCol.WindowBg, BG_WINDOW);
        col(s, ImGuiCol.ChildBg, 0, 0, 0, 0);
        col(s, ImGuiCol.PopupBg, BG_POPUP);
        col(s, ImGuiCol.Border, BORDER);
        col(s, ImGuiCol.BorderShadow, BORDER_SHADOW);

        col(s, ImGuiCol.FrameBg, FRAME);
        col(s, ImGuiCol.FrameBgHovered, FRAME_HOVER);
        col(s, ImGuiCol.FrameBgActive, FRAME_ACTIVE);

        col(s, ImGuiCol.TitleBg, TITLE);
        col(s, ImGuiCol.TitleBgActive, TITLE);
        col(s, ImGuiCol.TitleBgCollapsed, TITLE_COLLAPSED);
        col(s, ImGuiCol.MenuBarBg, MENU_BAR);

        col(s, ImGuiCol.ScrollbarBg, SCROLL_BG);
        col(s, ImGuiCol.ScrollbarGrab, GRAY_MED);
        col(s, ImGuiCol.ScrollbarGrabHovered, GRAY_LIGHT);
        col(s, ImGuiCol.ScrollbarGrabActive, GRAY_HI);

        col(s, ImGuiCol.CheckMark, ACCENT_GREEN);
        col(s, ImGuiCol.SliderGrab, SLIDER_GRAB);
        col(s, ImGuiCol.SliderGrabActive, ACCENT_GREEN);

        col(s, ImGuiCol.Button, BUTTON);
        col(s, ImGuiCol.ButtonHovered, BUTTON_ACTIVE);
        col(s, ImGuiCol.ButtonActive, BUTTON_ACTIVE);

        col(s, ImGuiCol.Header, HEADER);
        col(s, ImGuiCol.HeaderHovered, HEADER_SELECTION);
        col(s, ImGuiCol.HeaderActive, HEADER_SELECTION);

        col(s, ImGuiCol.Separator, SEPARATOR);
        col(s, ImGuiCol.SeparatorHovered, SEPARATOR_HOVER);
        col(s, ImGuiCol.SeparatorActive, SEPARATOR_ACTIVE);

        col(s, ImGuiCol.ResizeGrip, RESIZE_GRIP);
        col(s, ImGuiCol.ResizeGripHovered, RESIZE_GRIP_HOVER);
        col(s, ImGuiCol.ResizeGripActive, RESIZE_GRIP_ACTIVE);

        col(s, ImGuiCol.Tab, TAB);
        col(s, ImGuiCol.TabHovered, TAB_HOVER);
        col(s, ImGuiCol.TabActive, TAB_ACTIVE);
        col(s, ImGuiCol.TabUnfocused, TAB_UNFOCUSED);
        col(s, ImGuiCol.TabUnfocusedActive, TAB_UNFOCUSED_ACTIVE);

        col(s, ImGuiCol.DockingPreview, DOCKING_PREVIEW);
        col(s, ImGuiCol.DockingEmptyBg, DOCKING_BG);

        col(s, ImGuiCol.PlotLines, PLOT_LINES);
        col(s, ImGuiCol.PlotLinesHovered, PLOT_HOVER);
        col(s, ImGuiCol.PlotHistogram, PLOT_HIST);
        col(s, ImGuiCol.PlotHistogramHovered, PLOT_HIST_HOVER);

        col(s, ImGuiCol.TableHeaderBg, TABLE_BG);
        col(s, ImGuiCol.TableBorderStrong, TABLE_BORDER_STRONG);
        col(s, ImGuiCol.TableBorderLight, TABLE_BORDER_LIGHT);
        col(s, ImGuiCol.TableRowBg, 0, 0, 0, 0);
        col(s, ImGuiCol.TableRowBgAlt, 1, 1, 1, 0.06f);

        col(s, ImGuiCol.TextSelectedBg, SELECTION_BLUE);
        col(s, ImGuiCol.DragDropTarget, DRAG_DROP);
        col(s, ImGuiCol.NavHighlight, NAV_HIGHLIGHT);
        col(s, ImGuiCol.NavWindowingHighlight, 1, 1, 1, 0.70f);
        col(s, ImGuiCol.NavWindowingDimBg, DIM_BG);
        col(s, ImGuiCol.ModalWindowDimBg, MODAL_DIM);

        rounding(s, 1, 1, 1, 1, 1, 1, 1);
        borders(s, 1, 1, 1, 1, 1);
        padding(s, 5, 4);
        framePadding(s, 10, 5);
        itemSpacing(s, 7, 3);
        itemInnerSpacing(s, 4, 4);
        s.setCellPadding(4, 2);
        s.setIndentSpacing(14);
        s.setScrollbarSize(10);
        s.setGrabMinSize(10);
        s.setLogSliderDeadzone(4);
        s.setWindowMenuButtonPosition(ImGuiDir.Right);
    }
}