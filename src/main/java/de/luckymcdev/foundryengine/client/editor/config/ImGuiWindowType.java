package de.luckymcdev.foundryengine.client.editor.config;

import imgui.ImGui;

/**
 * The ImGui Window type.
 * Represents how a window is currently being handled.
 */
public enum ImGuiWindowType {
	/**
	 * Docked ImGui Window Type
	 * (Window is currently docked to a dockspace)
	 */
	DOCKED,
	/**
	 * Viewport ImGui Window Type
	 * (Window is outside the minecraft window)
	 */
	VIEWPORT,
	/**
	 * Window ImGui Window Type
	 * (Window is a movable object in the minecraft window)
	 */
	WINDOW;

	/**
	 * Get the Window type for a window.
	 * Example call:
	 * get(Instances.getWindow().handle())
	 *
	 * @param windowHandle the window handle
	 * @return The ImGui Window Type
	 */
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
