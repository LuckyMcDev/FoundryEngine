package de.luckymcdev.foundryengine.client.editor.styles.theme.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.theme.ImTheme;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public class CatpuccinMochaTheme implements ImTheme {
    @Override
    public String getName() {
        return "CatpuccinMocha";
    }

    /**
     * Applies the Catppuccin Mocha theme to the given ImGuiStyle.
     *
     * @param style The ImGuiStyle to apply the theme to.
     */
    @Override
    public void applyTheme(ImGuiStyle style) {
        // Base colors inspired by Catppuccin Mocha
        style.setColor(ImGuiCol.Text, 0.90f, 0.89f, 0.88f, 1.00f);         // Latte
        style.setColor(ImGuiCol.TextDisabled, 0.60f, 0.56f, 0.52f, 1.00f); // Surface2
        style.setColor(ImGuiCol.WindowBg, 0.17f, 0.14f, 0.20f, 1.00f);     // Base
        style.setColor(ImGuiCol.ChildBg, 0.18f, 0.16f, 0.22f, 1.00f);      // Mantle
        style.setColor(ImGuiCol.PopupBg, 0.17f, 0.14f, 0.20f, 1.00f);      // Base
        style.setColor(ImGuiCol.Border, 0.27f, 0.23f, 0.29f, 1.00f);       // Overlay0
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.FrameBg, 0.21f, 0.18f, 0.25f, 1.00f);              // Crust
        style.setColor(ImGuiCol.FrameBgHovered, 0.24f, 0.20f, 0.29f, 1.00f);       // Overlay1
        style.setColor(ImGuiCol.FrameBgActive, 0.26f, 0.22f, 0.31f, 1.00f);        // Overlay2
        style.setColor(ImGuiCol.TitleBg, 0.14f, 0.12f, 0.18f, 1.00f);              // Mantle
        style.setColor(ImGuiCol.TitleBgActive, 0.17f, 0.15f, 0.21f, 1.00f);        // Mantle
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.14f, 0.12f, 0.18f, 1.00f);     // Mantle
        style.setColor(ImGuiCol.MenuBarBg, 0.17f, 0.15f, 0.22f, 1.00f);            // Base
        style.setColor(ImGuiCol.ScrollbarBg, 0.17f, 0.14f, 0.20f, 1.00f);          // Base
        style.setColor(ImGuiCol.ScrollbarGrab, 0.21f, 0.18f, 0.25f, 1.00f);        // Crust
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.24f, 0.20f, 0.29f, 1.00f); // Overlay1
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.26f, 0.22f, 0.31f, 1.00f);  // Overlay2
        style.setColor(ImGuiCol.CheckMark, 0.95f, 0.66f, 0.47f, 1.00f);            // Peach
        style.setColor(ImGuiCol.SliderGrab, 0.82f, 0.61f, 0.85f, 1.00f);           // Lavender
        style.setColor(ImGuiCol.SliderGrabActive, 0.89f, 0.54f, 0.79f, 1.00f);     // Pink
        style.setColor(ImGuiCol.Button, 0.65f, 0.34f, 0.46f, 1.00f);               // Maroon
        style.setColor(ImGuiCol.ButtonHovered, 0.71f, 0.40f, 0.52f, 1.00f);        // Red
        style.setColor(ImGuiCol.ButtonActive, 0.76f, 0.46f, 0.58f, 1.00f);         // Pink
        style.setColor(ImGuiCol.Header, 0.65f, 0.34f, 0.46f, 1.00f);               // Maroon
        style.setColor(ImGuiCol.HeaderHovered, 0.71f, 0.40f, 0.52f, 1.00f);        // Red
        style.setColor(ImGuiCol.HeaderActive, 0.76f, 0.46f, 0.58f, 1.00f);         // Pink
        style.setColor(ImGuiCol.Separator, 0.27f, 0.23f, 0.29f, 1.00f);            // Overlay0
        style.setColor(ImGuiCol.SeparatorHovered, 0.95f, 0.66f, 0.47f, 1.00f);     // Peach
        style.setColor(ImGuiCol.SeparatorActive, 0.95f, 0.66f, 0.47f, 1.00f);      // Peach
        style.setColor(ImGuiCol.ResizeGrip, 0.82f, 0.61f, 0.85f, 1.00f);           // Lavender
        style.setColor(ImGuiCol.ResizeGripHovered, 0.89f, 0.54f, 0.79f, 1.00f);    // Pink
        style.setColor(ImGuiCol.ResizeGripActive, 0.92f, 0.61f, 0.85f, 1.00f);     // Mauve
        style.setColor(ImGuiCol.Tab, 0.21f, 0.18f, 0.25f, 1.00f);                  // Crust
        style.setColor(ImGuiCol.TabHovered, 0.82f, 0.61f, 0.85f, 1.00f);           // Lavender
        style.setColor(ImGuiCol.TabActive, 0.76f, 0.46f, 0.58f, 1.00f);            // Pink
        style.setColor(ImGuiCol.TabUnfocused, 0.18f, 0.16f, 0.22f, 1.00f);         // Mantle
        style.setColor(ImGuiCol.TabUnfocusedActive, 0.21f, 0.18f, 0.25f, 1.00f);   // Crust
        style.setColor(ImGuiCol.DockingPreview, 0.95f, 0.66f, 0.47f, 0.70f);       // Peach
        style.setColor(ImGuiCol.DockingEmptyBg, 0.12f, 0.12f, 0.12f, 1.00f);       // Base
        style.setColor(ImGuiCol.PlotLines, 0.82f, 0.61f, 0.85f, 1.00f);            // Lavender
        style.setColor(ImGuiCol.PlotLinesHovered, 0.89f, 0.54f, 0.79f, 1.00f);     // Pink
        style.setColor(ImGuiCol.PlotHistogram, 0.82f, 0.61f, 0.85f, 1.00f);        // Lavender
        style.setColor(ImGuiCol.PlotHistogramHovered, 0.89f, 0.54f, 0.79f, 1.00f); // Pink
        style.setColor(ImGuiCol.TableHeaderBg, 0.19f, 0.19f, 0.20f, 1.00f);        // Mantle
        style.setColor(ImGuiCol.TableBorderStrong, 0.27f, 0.23f, 0.29f, 1.00f);    // Overlay0
        style.setColor(ImGuiCol.TableBorderLight, 0.23f, 0.23f, 0.25f, 1.00f);     // Surface2
        style.setColor(ImGuiCol.TableRowBg, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.TableRowBgAlt, 1.00f, 1.00f, 1.00f, 0.06f);  // Surface0
        style.setColor(ImGuiCol.TextSelectedBg, 0.82f, 0.61f, 0.85f, 0.35f); // Lavender
        style.setColor(ImGuiCol.DragDropTarget, 0.95f, 0.66f, 0.47f, 0.90f); // Peach
        style.setColor(ImGuiCol.NavHighlight, 0.82f, 0.61f, 0.85f, 1.00f);   // Lavender
        style.setColor(ImGuiCol.NavWindowingHighlight, 1.00f, 1.00f, 1.00f, 0.70f);
        style.setColor(ImGuiCol.NavWindowingDimBg, 0.80f, 0.80f, 0.80f, 0.20f);
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.80f, 0.80f, 0.80f, 0.35f);

        // Style adjustments
        style.setWindowRounding(6.0f);
        style.setFrameRounding(4.0f);
        style.setGrabRounding(3.0f);
        style.setChildRounding(4.0f);

        style.setWindowTitleAlign(0.50f, 0.50f);
        style.setWindowPadding(8.0f, 8.0f);
        style.setFramePadding(5.0f, 4.0f);
        style.setItemSpacing(6.0f, 6.0f);
        style.setItemInnerSpacing(6.0f, 6.0f);

        style.setGrabMinSize(10.0f);

        style.setPopupRounding(3F);
        style.setScrollbarRounding(9F);

        style.setIndentSpacing(25F);
        style.setScrollbarSize(15F);
        style.setWindowBorderSize(0F);
        style.setSelectableTextAlign(0F, 0.5F);
        style.setAlpha(1F);

        style.setAntiAliasedLines(true);
        style.setAntiAliasedFill(true);
    }
}
