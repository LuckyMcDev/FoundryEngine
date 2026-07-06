package de.luckymcdev.foundryengine.client.imgui;

import de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGlfw;
import imgui.ImGui;
import imgui.flag.ImGuiKey;

public record ImGuiShortcut(int key, boolean ctrl, boolean shift, boolean alt) {

	// Static factories for clean construction
	public static ImGuiShortcut of(int key) {
		return new ImGuiShortcut(normalizeKey(key), false, false, false);
	}

	public static ImGuiShortcut ctrl(int key) {
		return new ImGuiShortcut(normalizeKey(key), true, false, false);
	}

	public static ImGuiShortcut shift(int key) {
		return new ImGuiShortcut(normalizeKey(key), false, true, false);
	}

	public static ImGuiShortcut alt(int key) {
		return new ImGuiShortcut(normalizeKey(key), false, false, true);
	}

	public static ImGuiShortcut ctrlShift(int key) {
		return new ImGuiShortcut(normalizeKey(key), true, true, false);
	}

	public static ImGuiShortcut ctrlAlt(int key) {
		return new ImGuiShortcut(normalizeKey(key), true, false, true);
	}

	public static ImGuiShortcut empty() {
		return new ImGuiShortcut(ImGuiKey.None, false, false, false);
	}

	private static int normalizeKey(int key) {
		return ImGuiImplGlfw.glfwKeyToImGuiKey(key);
	}

	/**
	 * Returns true on the frame this shortcut is pressed.
	 */
	public boolean isPressed() {
		if (key == ImGuiKey.None) {
			return false;
		}
		if (ctrl && !ImGui.isKeyDown(ImGuiKey.LeftCtrl) && !ImGui.isKeyDown(ImGuiKey.RightCtrl)) {
			return false;
		}
		if (shift && !ImGui.isKeyDown(ImGuiKey.LeftShift) && !ImGui.isKeyDown(ImGuiKey.RightShift)) {
			return false;
		}
		if (alt && !ImGui.isKeyDown(ImGuiKey.LeftAlt) && !ImGui.isKeyDown(ImGuiKey.RightAlt)) {
			return false;
		}
		return ImGui.isKeyPressed(key);
	}

	/**
	 * Formats a human-readable label like "Ctrl+P" for display in menus.
	 */
	public String toLabel() {
		StringBuilder sb = new StringBuilder();
		if (ctrl) {
			sb.append("Ctrl + ");
		}
		if (shift) {
			sb.append("Shift + ");
		}
		if (alt) {
			sb.append("Alt + ");
		}
		if (key != ImGuiKey.None) {
			String keyName = ImGui.getKeyName(key);
			if (keyName != null && !keyName.isBlank()) {
				sb.append(keyName);
			}
		}
		return sb.toString();
	}
}
