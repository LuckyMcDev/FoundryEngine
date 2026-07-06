package de.luckymcdev.foundryengine.client.imgui;

import imgui.flag.ImGuiStyleVar;

public interface ImStyleVarConsumer {
	void setStyleVar(int key, float value);

	void setStyleVar(int key, float x, float y);

	default void setAlpha(float value) {
		setStyleVar(ImGuiStyleVar.Alpha, value);
	}

	default void setDisabledAlpha(float value) {
		setStyleVar(ImGuiStyleVar.DisabledAlpha, value);
	}

	default void setWindowPadding(float x, float y) {
		setStyleVar(ImGuiStyleVar.WindowPadding, x, y);
	}

	default void setWindowRounding(float value) {
		setStyleVar(ImGuiStyleVar.WindowRounding, value);
	}

	default void setWindowBorderSize(float value) {
		setStyleVar(ImGuiStyleVar.WindowBorderSize, value);
	}

	default void setWindowMinSize(float x, float y) {
		setStyleVar(ImGuiStyleVar.WindowMinSize, x, y);
	}

	default void setWindowTitleAlign(float x, float y) {
		setStyleVar(ImGuiStyleVar.WindowTitleAlign, x, y);
	}

	default void setChildRounding(float value) {
		setStyleVar(ImGuiStyleVar.ChildRounding, value);
	}

	default void setChildBorderSize(float value) {
		setStyleVar(ImGuiStyleVar.ChildBorderSize, value);
	}

	default void setPopupRounding(float value) {
		setStyleVar(ImGuiStyleVar.PopupRounding, value);
	}

	default void setPopupBorderSize(float value) {
		setStyleVar(ImGuiStyleVar.PopupBorderSize, value);
	}

	default void setFramePadding(float x, float y) {
		setStyleVar(ImGuiStyleVar.FramePadding, x, y);
	}

	default void setFrameRounding(float value) {
		setStyleVar(ImGuiStyleVar.FrameRounding, value);
	}

	default void setFrameBorderSize(float value) {
		setStyleVar(ImGuiStyleVar.FrameBorderSize, value);
	}

	default void setItemSpacing(float x, float y) {
		setStyleVar(ImGuiStyleVar.ItemSpacing, x, y);
	}

	default void setItemInnerSpacing(float x, float y) {
		setStyleVar(ImGuiStyleVar.ItemInnerSpacing, x, y);
	}

	default void setCellPadding(float x, float y) {
		setStyleVar(ImGuiStyleVar.CellPadding, x, y);
	}

	default void setIndentSpacing(float value) {
		setStyleVar(ImGuiStyleVar.IndentSpacing, value);
	}

	default void setScrollbarSize(float value) {
		setStyleVar(ImGuiStyleVar.ScrollbarSize, value);
	}

	default void setScrollbarRounding(float value) {
		setStyleVar(ImGuiStyleVar.ScrollbarRounding, value);
	}

	default void setGrabMinSize(float value) {
		setStyleVar(ImGuiStyleVar.GrabMinSize, value);
	}

	default void setGrabRounding(float value) {
		setStyleVar(ImGuiStyleVar.GrabRounding, value);
	}

	default void setTabRounding(float value) {
		setStyleVar(ImGuiStyleVar.TabRounding, value);
	}

	default void setButtonTextAlign(float x, float y) {
		setStyleVar(ImGuiStyleVar.ButtonTextAlign, x, y);
	}

	default void setSelectableTextAlign(float x, float y) {
		setStyleVar(ImGuiStyleVar.SelectableTextAlign, x, y);
	}
}
