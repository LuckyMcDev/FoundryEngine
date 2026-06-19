package de.luckymcdev.foundryengine.client.editor.panel.editor;

import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import imgui.ImGui;

/**
 * WIP
 * A Panel for the editor which will be to generate bundles from an ingame menu.
 */
public class EditorPanel extends Panel {
    private String statusMessage = "";
    private long statusExpiry = 0L;

    protected EditorPanel(Builder builder) {
        super(builder);
    }

    protected void setStatus(String message) {
        statusMessage = message;
        statusExpiry = System.currentTimeMillis() + 4000L;
    }

    private float getStatusReservedHeight() {
        if (!statusMessage.isEmpty() && System.currentTimeMillis() <= statusExpiry) {
            return ImGui.getTextLineHeightWithSpacing() + ImGui.getStyle().getItemSpacingY() * 2;
        }
        return 0;
    }

    protected void beginContent() {
        ImGui.beginChild("##editor_content_scroll", 0, -getStatusReservedHeight(), false);
    }

    protected void endContent() {
        ImGui.endChild();
        renderStatus();
    }

    private void renderStatus() {
        if (statusMessage.isEmpty()) return;
        if (System.currentTimeMillis() > statusExpiry) {
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
