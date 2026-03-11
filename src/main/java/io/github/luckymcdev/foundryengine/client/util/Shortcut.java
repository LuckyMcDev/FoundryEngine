package io.github.luckymcdev.foundryengine.client.util;

import imgui.ImGui;
import imgui.flag.ImGuiKey;

public record Shortcut(int key, boolean ctrl, boolean shift, boolean alt) {

    // Static factories for clean construction
    public static Shortcut of(int key) {
        return new Shortcut(key, false, false, false);
    }

    public static Shortcut ctrl(int key) {
        return new Shortcut(key, true, false, false);
    }

    public static Shortcut shift(int key) {
        return new Shortcut(key, false, true, false);
    }

    public static Shortcut alt(int key) {
        return new Shortcut(key, false, false, true);
    }

    public static Shortcut ctrlShift(int key) {
        return new Shortcut(key, true, true, false);
    }

    public static Shortcut ctrlAlt(int key) {
        return new Shortcut(key, true, false, true);
    }

    public static Shortcut empty() {
        return new Shortcut(ImGuiKey.None, false, false, false);
    }

    /**
     * Returns true on the frame this shortcut is pressed.
     */
    public boolean isPressed() {
        if (ctrl && !ImGui.isKeyDown(ImGuiKey.LeftCtrl) && !ImGui.isKeyDown(ImGuiKey.RightCtrl)) return false;
        if (shift && !ImGui.isKeyDown(ImGuiKey.LeftShift) && !ImGui.isKeyDown(ImGuiKey.RightShift)) return false;
        if (alt && !ImGui.isKeyDown(ImGuiKey.LeftAlt) && !ImGui.isKeyDown(ImGuiKey.RightAlt)) return false;
        return ImGui.isKeyPressed(key);
    }

    /**
     * Formats a human-readable label like "Ctrl+P" for display in menus.
     */
    public String toLabel() {
        StringBuilder sb = new StringBuilder();
        if (ctrl) sb.append("Ctrl + ");
        if (shift) sb.append("Shift + ");
        if (alt) sb.append("Alt + ");
        sb.append(ImGui.getKeyName(key));
        return sb.toString();
    }
}
