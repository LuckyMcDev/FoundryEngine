package de.luckymcdev.foundryengine.client.editor.builtin.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.resources.Identifier;

/**
 * TODO: dynamic renering of all effects.
 */
public class EffectPanel extends EditorPanel {
    public static final EffectPanel INSTANCE = new EffectPanel();

    public EffectPanel() {
        super(Common.id("effect_panel"), "Effects", ImIcons.FA.FA_VIDEO_CAMERA, Shortcut.empty());
        this.category = PanelCategory.EDITOR_TOOLS;
    }

    @Override
    public void content() {
        ImGui.text("Post Processing Effects");

        renderEffectToggle("Blur", Identifier.withDefaultNamespace("blur"), 100);
        renderEffectToggle("Invert", Identifier.withDefaultNamespace("invert"), 50);
        renderEffectToggle("Creeper", Identifier.withDefaultNamespace("creeper"), 10);
        renderEffectToggle("Spider", Identifier.withDefaultNamespace("spider"), 10);
        renderEffectToggle("Greyscale Test", Common.id("grayscale"), 100);

        if (ImGui.button("Reset All Effects")) {
            Client.getEffectManager().clearAllEffects();
        }
    }


    /**
     * Helper to render a checkbox that communicates with the EffectManager
     */
    private void renderEffectToggle(String label, Identifier id, int priority) {
        boolean active = Client.getEffectManager().getActiveEffects().contains(id);
        ImBoolean check = new ImBoolean(active);
        if (ImGui.checkbox(label + " (Priority: " + priority + ")", check)) {
            Client.getEffectManager().setEffectActive(id, priority, check.get());
        }
    }
}
