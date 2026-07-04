package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.post.internal.PostEffectEntry;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

import java.util.List;

public class EffectPanel extends EditorPanel {
    public static final EffectPanel INSTANCE = new EffectPanel();

    public EffectPanel() {
        super(new Builder(Common.id("effect_panel"))
                .icon(ImIcons.FA.FA_SLIDERS)
                .category(PanelCategory.TOOLS));
    }

    @Override
    public void content(ImGraphicsExtractor g) {
        var mgr = Client.getPostEffectManager();
        List<PostEffectEntry> entries = mgr.getRegistry().getEntries();

        int enabledCount = (int) entries.stream().filter(PostEffectEntry::isEnabled).count();

        g.cardBegin("##effects_header");
        ImGui.text(ImIcons.FA.FA_SLIDERS + "  Post Processing Effects");
        ImGui.sameLine();
        ImGui.textDisabled("(" + enabledCount + "/" + entries.size() + " active)");
        g.cardEnd();

        ImGui.spacing();

        if (entries.isEmpty()) {
            g.centeredMessage("No post-processing effects registered.");
            return;
        }

        for (var entry : entries) {
            renderEffectCard(entry);
        }

        ImGui.dummy(0, 4);

        int activeCount = (int) entries.stream().filter(PostEffectEntry::isActive).count();
        if (activeCount > 0) {
            g.section("Active Effects");
            for (var entry : entries) {
                if (entry.isEnabled()) {
                    renderActiveIndicator(entry);
                }
            }
        }
    }

    private void renderEffectCard(PostEffectEntry entry) {
        boolean enabled = entry.isEnabled();
        String path = entry.getId().getPath();

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 8, 6);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, enabled ? 0x1A4CAF50 : 0x1A666666);

        ImGui.beginChild("##effect_" + path, 0, ImGui.getTextLineHeightWithSpacing() + 20, true);
        ImGui.beginGroup();

        if (ImGui.checkbox("##toggle_" + path, enabled)) {
            entry.setEnabled(!enabled);
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(enabled ? "Disable effect" : "Enable effect");
        }

        ImGui.sameLine();
        ImGui.textColored(enabled ? 0xFFCCCCCC : 0xFF666666, path);

        ImGui.sameLine(ImGui.getContentRegionAvailX() + ImGui.getCursorPosX() - 16);
        if (entry.isActive()) {
            ImGui.textColored(0xFF4CAF50, ImGraphicsExtractor.icon(ImIcons.FA.FA_CIRCLE));
            if (ImGui.isItemHovered()) ImGui.setTooltip("Effect is active");
        } else if (enabled) {
            ImGui.textDisabled(ImGraphicsExtractor.icon(ImIcons.FA.FA_CIRCLE));
            if (ImGui.isItemHovered()) ImGui.setTooltip("Effect is loaded but not currently active");
        }

        ImGui.endGroup();
        ImGui.endChild();

        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }

    private void renderActiveIndicator(PostEffectEntry entry) {
        String status = entry.isActive() ? "Active" : "Inactive";
        int color = entry.isActive() ? 0xFF4CAF50 : 0xFF888888;
        ImGui.textColored(color, "  " + ImGraphicsExtractor.icon(ImIcons.FA.FA_CIRCLE) + " " + status + " \u00b7 " + entry.getId().getPath());
    }
}
