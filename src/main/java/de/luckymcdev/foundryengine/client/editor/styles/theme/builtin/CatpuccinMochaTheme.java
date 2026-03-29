package de.luckymcdev.foundryengine.client.editor.styles.theme.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.theme.ImTheme;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public class CatpuccinMochaTheme implements ImTheme {

    private static final Color BASE = new Color(0.17f, 0.14f, 0.20f, 1.00f);
    private static final Color MANTLE = new Color(0.18f, 0.16f, 0.22f, 1.00f);
    private static final Color CRUST = new Color(0.21f, 0.18f, 0.25f, 1.00f);
    private static final Color SURFACE0 = new Color(0.23f, 0.23f, 0.25f, 1.00f);
    private static final Color OVERLAY0 = new Color(0.27f, 0.23f, 0.29f, 1.00f);
    private static final Color OVERLAY1 = new Color(0.24f, 0.20f, 0.29f, 1.00f);
    private static final Color OVERLAY2 = new Color(0.26f, 0.22f, 0.31f, 1.00f);
    private static final Color TEXT = new Color(0.90f, 0.89f, 0.88f, 1.00f);
    private static final Color SUBTEXT = new Color(0.60f, 0.56f, 0.52f, 1.00f);
    private static final Color PEACH = new Color(0.95f, 0.66f, 0.47f, 1.00f);
    private static final Color LAVENDER = new Color(0.82f, 0.61f, 0.85f, 1.00f);
    private static final Color PINK = new Color(0.89f, 0.54f, 0.79f, 1.00f);
    private static final Color MAUVE = new Color(0.92f, 0.61f, 0.85f, 1.00f);
    private static final Color MAROON = new Color(0.65f, 0.34f, 0.46f, 1.00f);
    private static final Color RED = new Color(0.71f, 0.40f, 0.52f, 1.00f);

    @Override
    public String getName() {
        return "Catpuccin Mocha";
    }

    @Override
    public void applyTheme(ImGuiStyle s) {
        col(s, ImGuiCol.Text, TEXT);
        col(s, ImGuiCol.TextDisabled, SUBTEXT);

        col(s, ImGuiCol.WindowBg, BASE);
        col(s, ImGuiCol.ChildBg, MANTLE);
        col(s, ImGuiCol.PopupBg, BASE);

        col(s, ImGuiCol.Border, OVERLAY0);
        col(s, ImGuiCol.BorderShadow, 0, 0, 0, 0);

        col(s, ImGuiCol.FrameBg, CRUST);
        col(s, ImGuiCol.FrameBgHovered, OVERLAY1);
        col(s, ImGuiCol.FrameBgActive, OVERLAY2);

        col(s, ImGuiCol.TitleBg, MANTLE);
        col(s, ImGuiCol.TitleBgActive, MANTLE);
        col(s, ImGuiCol.TitleBgCollapsed, MANTLE);
        col(s, ImGuiCol.MenuBarBg, BASE);

        col(s, ImGuiCol.ScrollbarBg, BASE);
        col(s, ImGuiCol.ScrollbarGrab, CRUST);
        col(s, ImGuiCol.ScrollbarGrabHovered, OVERLAY1);
        col(s, ImGuiCol.ScrollbarGrabActive, OVERLAY2);

        col(s, ImGuiCol.CheckMark, PEACH);
        col(s, ImGuiCol.SliderGrab, LAVENDER);
        col(s, ImGuiCol.SliderGrabActive, PINK);

        col(s, ImGuiCol.Button, MAROON);
        col(s, ImGuiCol.ButtonHovered, RED);
        col(s, ImGuiCol.ButtonActive, PINK);

        col(s, ImGuiCol.Header, MAROON);
        col(s, ImGuiCol.HeaderHovered, RED);
        col(s, ImGuiCol.HeaderActive, PINK);

        col(s, ImGuiCol.Separator, OVERLAY0);
        col(s, ImGuiCol.SeparatorHovered, PEACH);
        col(s, ImGuiCol.SeparatorActive, PEACH);

        col(s, ImGuiCol.ResizeGrip, LAVENDER);
        col(s, ImGuiCol.ResizeGripHovered, PINK);
        col(s, ImGuiCol.ResizeGripActive, MAUVE);

        col(s, ImGuiCol.Tab, CRUST);
        col(s, ImGuiCol.TabHovered, LAVENDER);
        col(s, ImGuiCol.TabActive, PINK);
        col(s, ImGuiCol.TabUnfocused, MANTLE);
        col(s, ImGuiCol.TabUnfocusedActive, CRUST);

        col(s, ImGuiCol.DockingPreview, PEACH.r(), PEACH.g(), PEACH.b(), 0.70f);
        col(s, ImGuiCol.DockingEmptyBg, 0.12f, 0.12f, 0.12f);

        col(s, ImGuiCol.PlotLines, LAVENDER);
        col(s, ImGuiCol.PlotLinesHovered, PINK);
        col(s, ImGuiCol.PlotHistogram, LAVENDER);
        col(s, ImGuiCol.PlotHistogramHovered, PINK);

        col(s, ImGuiCol.TableHeaderBg, MANTLE);
        col(s, ImGuiCol.TableBorderStrong, OVERLAY0);
        col(s, ImGuiCol.TableBorderLight, SURFACE0);
        col(s, ImGuiCol.TableRowBg, 0, 0, 0, 0);
        col(s, ImGuiCol.TableRowBgAlt, 1, 1, 1, 0.06f);

        col(s, ImGuiCol.TextSelectedBg, LAVENDER.r(), LAVENDER.g(), LAVENDER.b(), 0.35f);
        col(s, ImGuiCol.DragDropTarget, PEACH.r(), PEACH.g(), PEACH.b(), 0.90f);
        col(s, ImGuiCol.NavHighlight, LAVENDER);
        col(s, ImGuiCol.NavWindowingHighlight, 1, 1, 1, 0.70f);
        col(s, ImGuiCol.NavWindowingDimBg, 0.80f, 0.80f, 0.80f, 0.20f);
        col(s, ImGuiCol.ModalWindowDimBg, 0.80f, 0.80f, 0.80f, 0.35f);

        rounding(s, 6, 4, 3, 4, 3, 9, 4);
        borders(s, 0, 0, 0, 0, 0);
        padding(s, 8, 8);
        framePadding(s, 5, 4);
        itemSpacing(s, 6, 6);
        itemInnerSpacing(s, 6, 6);
        s.setGrabMinSize(10);
        s.setIndentSpacing(25);
        s.setScrollbarSize(15);
        s.setSelectableTextAlign(0, 0.5f);
        s.setWindowTitleAlign(0.5f, 0.5f);
        s.setAntiAliasedLines(true);
        s.setAntiAliasedFill(true);
    }
}