package de.luckymcdev.foundryengine.client.editor.styles.theme.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.theme.ImTheme;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public class BessDarkTheme implements ImTheme {

    @Override
    public String getName() {
        return "BessDark";
    }


    /**
     * Applies the BessDark theme to the given ImGuiStyle.
     *
     * @param style The ImGuiStyle to apply the theme to.
     */
    @Override
    public void applyTheme(ImGuiStyle style) {
        // Primary background
        style.setColor(ImGuiCol.WindowBg, 0.07f, 0.07f, 0.09f, 1.00f);  // #131318
        style.setColor(ImGuiCol.MenuBarBg, 0.12f, 0.12f, 0.15f, 1.00f); // #131318
        style.setColor(ImGuiCol.PopupBg, 0.18f, 0.18f, 0.22f, 1.00f);

        // Headers
        style.setColor(ImGuiCol.Header, 0.18f, 0.18f, 0.22f, 1.00f);
        style.setColor(ImGuiCol.HeaderHovered, 0.30f, 0.30f, 0.40f, 1.00f);
        style.setColor(ImGuiCol.HeaderActive, 0.25f, 0.25f, 0.35f, 1.00f);

        // Buttons
        style.setColor(ImGuiCol.Button, 0.20f, 0.22f, 0.27f, 1.00f);
        style.setColor(ImGuiCol.ButtonHovered, 0.30f, 0.32f, 0.40f, 1.00f);
        style.setColor(ImGuiCol.ButtonActive, 0.35f, 0.38f, 0.50f, 1.00f);

        // Frame BG
        style.setColor(ImGuiCol.FrameBg, 0.15f, 0.15f, 0.18f, 1.00f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.22f, 0.22f, 0.27f, 1.00f);
        style.setColor(ImGuiCol.FrameBgActive, 0.25f, 0.25f, 0.30f, 1.00f);

        // Tabs
        style.setColor(ImGuiCol.Tab, 0.18f, 0.18f, 0.22f, 1.00f);
        style.setColor(ImGuiCol.TabHovered, 0.35f, 0.35f, 0.50f, 1.00f);
        style.setColor(ImGuiCol.TabActive, 0.25f, 0.25f, 0.38f, 1.00f);
        style.setColor(ImGuiCol.TabUnfocused, 0.13f, 0.13f, 0.17f, 1.00f);
        style.setColor(ImGuiCol.TabUnfocusedActive, 0.20f, 0.20f, 0.25f, 1.00f);

        // Title
        style.setColor(ImGuiCol.TitleBg, 0.12f, 0.12f, 0.15f, 1.00f);
        style.setColor(ImGuiCol.TitleBgActive, 0.15f, 0.15f, 0.20f, 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.10f, 0.10f, 0.12f, 1.00f);

        // Borders
        style.setColor(ImGuiCol.Border, 0.20f, 0.20f, 0.25f, 0.50f);
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f);

        // Text
        style.setColor(ImGuiCol.Text, 0.90f, 0.90f, 0.95f, 1.00f);
        style.setColor(ImGuiCol.TextDisabled, 0.50f, 0.50f, 0.55f, 1.00f);

        // Highlights
        style.setColor(ImGuiCol.CheckMark, 0.50f, 0.70f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.SliderGrab, 0.50f, 0.70f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.60f, 0.80f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.ResizeGrip, 0.50f, 0.70f, 1.00f, 0.50f);
        style.setColor(ImGuiCol.ResizeGripHovered, 0.60f, 0.80f, 1.00f, 0.75f);
        style.setColor(ImGuiCol.ResizeGripActive, 0.70f, 0.90f, 1.00f, 1.00f);

        // Scrollbar
        style.setColor(ImGuiCol.ScrollbarBg, 0.10f, 0.10f, 0.12f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.30f, 0.30f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.40f, 0.40f, 0.50f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.45f, 0.45f, 0.55f, 1.00f);

        // Style tweaks
        style.setWindowRounding(5.0f);
        style.setFrameRounding(5.0f);
        style.setGrabRounding(5.0f);
        style.setTabRounding(5.0f);
        style.setPopupRounding(5.0f);
        style.setScrollbarRounding(5.0f);
        style.setWindowPadding(10, 10);
        style.setFramePadding(6, 4);
        style.setItemSpacing(8, 6);
        style.setPopupBorderSize(0.0f);
    }
}