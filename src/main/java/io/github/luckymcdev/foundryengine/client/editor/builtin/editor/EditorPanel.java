package io.github.luckymcdev.foundryengine.client.editor.builtin.editor;

import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.util.key.Shortcut;
import net.minecraft.resources.Identifier;

/**
 * WIP
 * A Panel for the editor which will be to generate bundles from an ingame menu.
 */
public class EditorPanel extends Panel {
    protected EditorPanel(Identifier id, String label) {
        super(id, label);
    }
    protected EditorPanel(Identifier id, String label, Shortcut shortcut) {
        super(id, label, shortcut);
    }
}
