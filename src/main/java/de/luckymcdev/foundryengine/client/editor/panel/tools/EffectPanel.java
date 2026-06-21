package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.post.internal.PostEffectEntry;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;

import java.util.List;

public class EffectPanel extends EditorPanel {
    public static final EffectPanel INSTANCE = new EffectPanel();

    public EffectPanel() {
        super(new Builder(Common.id("effect_panel"), "Effects")
                .icon(ImIcons.FA.FA_VIDEO_CAMERA)
                .category(PanelCategory.TOOLS));
    }

    @Override
    public void content() {
        var mgr = Client.getPostEffectManager();

        ImGuiUtils.section("Post Processing Effects");

        List<PostEffectEntry> entries = mgr.getRegistry().getEntries();
        for (var entry : entries) {
            renderEffectToggle(entry);
        }

        ImGui.dummy(0, 4);

        ImGuiUtils.section("Active Effects");
        for (var entry : entries) {
            renderActive(entry);
        }
    }

    private void renderEffectToggle(PostEffectEntry entry) {
        boolean enabled = entry.isEnabled();
        if (ImGui.checkbox(entry.getId().getPath(), enabled)) {
            entry.setEnabled(!enabled);
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Toggle post-processing effect");
        }
    }

    private void renderActive(PostEffectEntry entry) {
        if (!entry.isEnabled()) return;
        String status = entry.isActive() ? "Active" : "Inactive";
        ImGui.textColored(entry.isActive() ? 0xFF00FF00 : 0xFFAAAAAA, "  " + status + " \u00b7 " + entry.getId().getPath());
    }
}
