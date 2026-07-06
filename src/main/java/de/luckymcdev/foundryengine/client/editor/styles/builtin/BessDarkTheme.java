package de.luckymcdev.foundryengine.client.editor.styles.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

public class BessDarkTheme implements ImTheme {

	private static final Color BG_DARK = new Color(0.07f, 0.07f, 0.09f, 1.00f);
	private static final Color BG_MED = new Color(0.12f, 0.12f, 0.15f, 1.00f);
	private static final Color BG_LIGHT = new Color(0.18f, 0.18f, 0.22f, 1.00f);
	private static final Color TEXT = new Color(0.90f, 0.90f, 0.95f, 1.00f);
	private static final Color TEXT_DISABLED = new Color(0.50f, 0.50f, 0.55f, 1.00f);
	private static final Color SELECTION = new Color(0.30f, 0.30f, 0.40f, 1.00f);
	private static final Color SELECTION_ACTIVE = new Color(0.25f, 0.25f, 0.35f, 1.00f);
	private static final Color BUTTON = new Color(0.20f, 0.22f, 0.27f, 1.00f);
	private static final Color BUTTON_HOVER = new Color(0.30f, 0.32f, 0.40f, 1.00f);
	private static final Color BUTTON_ACTIVE = new Color(0.35f, 0.38f, 0.50f, 1.00f);
	private static final Color FRAME = new Color(0.15f, 0.15f, 0.18f, 1.00f);
	private static final Color FRAME_HOVER = new Color(0.22f, 0.22f, 0.27f, 1.00f);
	private static final Color FRAME_ACTIVE = new Color(0.25f, 0.25f, 0.30f, 1.00f);
	private static final Color TAB_HOVER = new Color(0.35f, 0.35f, 0.50f, 1.00f);
	private static final Color TAB_ACTIVE = new Color(0.25f, 0.25f, 0.38f, 1.00f);
	private static final Color TAB_UNFOCUSED = new Color(0.13f, 0.13f, 0.17f, 1.00f);
	private static final Color TAB_UNFOCUSED_ACTIVE = new Color(0.20f, 0.20f, 0.25f, 1.00f);
	private static final Color TITLE_ACTIVE = new Color(0.15f, 0.15f, 0.20f, 1.00f);
	private static final Color TITLE_COLLAPSED = new Color(0.10f, 0.10f, 0.12f, 1.00f);
	private static final Color BORDER = new Color(0.20f, 0.20f, 0.25f, 0.50f);
	private static final Color ACCENT = new Color(0.50f, 0.70f, 1.00f, 1.00f);
	private static final Color ACCENT_HOVER = new Color(0.60f, 0.80f, 1.00f, 1.00f);
	private static final Color ACCENT_ACTIVE = new Color(0.70f, 0.90f, 1.00f, 1.00f);
	private static final Color SCROLL_GRAB = new Color(0.30f, 0.30f, 0.35f, 1.00f);
	private static final Color SCROLL_HOVER = new Color(0.40f, 0.40f, 0.50f, 1.00f);
	private static final Color SCROLL_ACTIVE = new Color(0.45f, 0.45f, 0.55f, 1.00f);

	@Override
	public String getName() {
		return "Bess Dark";
	}

	@Override
	public void applyTheme(ImGuiStyle s) {
		col(s, ImGuiCol.WindowBg, BG_DARK);
		col(s, ImGuiCol.MenuBarBg, BG_MED);
		col(s, ImGuiCol.PopupBg, BG_LIGHT);

		col(s, ImGuiCol.Text, TEXT);
		col(s, ImGuiCol.TextDisabled, TEXT_DISABLED);

		col(s, ImGuiCol.Header, BG_LIGHT);
		col(s, ImGuiCol.HeaderHovered, SELECTION);
		col(s, ImGuiCol.HeaderActive, SELECTION_ACTIVE);

		col(s, ImGuiCol.Button, BUTTON);
		col(s, ImGuiCol.ButtonHovered, BUTTON_HOVER);
		col(s, ImGuiCol.ButtonActive, BUTTON_ACTIVE);

		col(s, ImGuiCol.FrameBg, FRAME);
		col(s, ImGuiCol.FrameBgHovered, FRAME_HOVER);
		col(s, ImGuiCol.FrameBgActive, FRAME_ACTIVE);

		col(s, ImGuiCol.Tab, BG_LIGHT);
		col(s, ImGuiCol.TabHovered, TAB_HOVER);
		col(s, ImGuiCol.TabActive, TAB_ACTIVE);
		col(s, ImGuiCol.TabUnfocused, TAB_UNFOCUSED);
		col(s, ImGuiCol.TabUnfocusedActive, TAB_UNFOCUSED_ACTIVE);

		col(s, ImGuiCol.TitleBg, BG_MED);
		col(s, ImGuiCol.TitleBgActive, TITLE_ACTIVE);
		col(s, ImGuiCol.TitleBgCollapsed, TITLE_COLLAPSED);

		col(s, ImGuiCol.Border, BORDER);
		col(s, ImGuiCol.BorderShadow, 0, 0, 0, 0);

		col(s, ImGuiCol.CheckMark, ACCENT);
		col(s, ImGuiCol.SliderGrab, ACCENT);
		col(s, ImGuiCol.SliderGrabActive, ACCENT_HOVER);
		col(s, ImGuiCol.ResizeGrip, ACCENT.r(), ACCENT.g(), ACCENT.b(), 0.50f);
		col(s, ImGuiCol.ResizeGripHovered, ACCENT_HOVER.r(), ACCENT_HOVER.g(), ACCENT_HOVER.b(), 0.75f);
		col(s, ImGuiCol.ResizeGripActive, ACCENT_ACTIVE);

		col(s, ImGuiCol.ScrollbarBg, TITLE_COLLAPSED);
		col(s, ImGuiCol.ScrollbarGrab, SCROLL_GRAB);
		col(s, ImGuiCol.ScrollbarGrabHovered, SCROLL_HOVER);
		col(s, ImGuiCol.ScrollbarGrabActive, SCROLL_ACTIVE);

		rounding(s, 5, 5, 5, 5, 5, 5, 0);
		borders(s, 0, 0, 0, 0, 0);
		padding(s, 10, 10);
		framePadding(s, 6, 4);
		itemSpacing(s, 8, 6);
	}
}