package de.luckymcdev.foundryengine.client.editor.builtin;

import de.luckymcdev.foundryengine.client.editor.Panel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import net.minecraft.resources.Identifier;

/**
 * WIP
 * A Panel for the editor which will be to generate bundles from an ingame menu.
 */
public class EditorPanel extends Panel {
    protected EditorPanel(Identifier id, String label) {
        super(id, label);
    }

    protected EditorPanel(Identifier id, String label, ImIcon icon, Shortcut shortcut) {
        super(id, label, icon, shortcut);
        this.category = PanelCategory.EDITOR;
    }
}
