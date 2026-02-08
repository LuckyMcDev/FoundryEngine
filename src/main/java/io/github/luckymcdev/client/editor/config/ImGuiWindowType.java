package io.github.luckymcdev.client.editor.config;

import imgui.ImGui;

public enum ImGuiWindowType {
    DOCKED,
    VIEWPORT,
    WINDOW;

    public static ImGuiWindowType get(long windowHandle) {
        if (ImGui.getWindowViewport() == null) {
            return ImGuiWindowType.WINDOW;
        } else if (ImGui.getWindowViewport().getPlatformHandle() != windowHandle) {
            return ImGuiWindowType.WINDOW;
        } else if (ImGui.isWindowDocked()) {
            return ImGuiWindowType.DOCKED;
        } else {
            return ImGuiWindowType.VIEWPORT;
        }
    }

}
