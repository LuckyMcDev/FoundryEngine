package de.luckymcdev.foundryengine.client.editor.panel.editor;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import imgui.ImGui;
import net.minecraft.resources.Identifier;

/**
 * WIP
 * A Panel for the editor which will be to generate bundles from an ingame menu.
 */
public class EditorPanel extends Panel {
    private String statusMessage = "";
    private long statusExpiry = 0L;

    protected EditorPanel(Identifier id, String label, ImIcon icon, Shortcut shortcut, PanelCategory category) {
        super(id, label, icon, shortcut, category);
    }

    protected EditorPanel(Identifier id, String label, ImIcon icon, PanelCategory category) {
        super(id, label, icon, category);
    }

    protected EditorPanel(Identifier id, String label, ImIcon icon) {
        this(id, label, icon, PanelCategory.EDITOR);
    }

    protected EditorPanel(Identifier id, String label, ImIcon icon, Shortcut shortcut) {
        this(id, label, icon, shortcut, PanelCategory.EDITOR);
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
