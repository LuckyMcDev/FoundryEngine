package de.luckymcdev.foundryengine.client.editor.panel.editor;

import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import imgui.ImGui;

public class EditorPanel extends Panel {
	private String statusMessage = "";
	private long statusExpiry = 0L;
	private boolean beganChild = false;

	protected EditorPanel(Builder builder) {
		super(builder);
	}

	@Override
	public void content(ImGraphicsExtractor g) {
	}

	protected final void setStatus(String message) {
		statusMessage = message;
		statusExpiry = System.currentTimeMillis() + 4000L;
	}

	@Override
	protected void onPreContent() {
		if (hasStatus() && !hasMenuBar()) {
			ImGui.beginChild("##editor_content_scroll", 0, -getStatusReservedHeight(), false);
			beganChild = true;
		} else {
			beganChild = false;
		}
	}

	@Override
	protected void onPostContent() {
		if (beganChild) {
			ImGui.endChild();
		}
		renderStatus();
	}

	private boolean hasStatus() {
		return !statusMessage.isEmpty() && System.currentTimeMillis() <= statusExpiry;
	}

	private float getStatusReservedHeight() {
		return ImGui.getTextLineHeightWithSpacing() + ImGui.getStyle().getItemSpacingY() * 2;
	}

	private void renderStatus() {
		if (!hasStatus()) {
			statusMessage = "";
			return;
		}

		ImGui.separator();
		float availWidth = ImGui.getContentRegionAvailX();
		float textWidth = ImGui.calcTextSize(statusMessage).x;
		if (textWidth > availWidth) {
			ImGui.textWrapped(statusMessage);
		} else {
			ImGui.textDisabled(statusMessage);
		}
	}
}
