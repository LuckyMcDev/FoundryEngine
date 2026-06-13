package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.post.PrioritizedEffect;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.resources.Identifier;

import java.util.Locale;

public class EffectPanel extends EditorPanel {
    public static final EffectPanel INSTANCE = new EffectPanel();

    public EffectPanel() {
        super(Common.id("effect_panel"), "Effects", ImIcons.FA.FA_VIDEO_CAMERA, PanelCategory.TOOLS);
    }

    @Override
    public void content() {
        ImGuiUtils.section("Post Processing Effects");

        if (ImGui.button(ImIcons.FA.FA_ARROW_ROTATE_RIGHT + " Reload")) {
            Client.getEffectManager().reload();
        }

        for (PrioritizedEffect effect : Client.getEffectManager().getEffects()) {
            renderEffectToggle(effect.id().getPath().toUpperCase(Locale.ROOT), effect.id(), effect.priority());
        }

        if (ImGui.button("Reset All Effects")) {
            Client.getEffectManager().clearAllEffects();
        }
    }

    private void renderEffectToggle(String label, Identifier id, int priority) {
        boolean active = Client.getEffectManager().getActiveEffects().contains(id);
        ImBoolean check = new ImBoolean(active);
        if (ImGui.checkbox(label + " (Priority: " + priority + ")", check)) {
            Client.getEffectManager().setEffectActive(id, priority, check.get());
        }
    }
}
