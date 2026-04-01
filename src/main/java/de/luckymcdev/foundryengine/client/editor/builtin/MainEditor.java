package de.luckymcdev.foundryengine.client.editor.builtin;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import imgui.flag.ImGuiKey;

/**
 * The Main Editor which has all the functionality for the generation
 * see {@link EditorPanel}
 */
public class MainEditor extends EditorPanel {
    public static final MainEditor INSTANCE = new MainEditor();
    private MainEditor() {
        super(Common.id("main_editor"), "Main Editor", ImIcons.FA.FA_EDIT, Shortcut.ctrl(ImGuiKey.F9));
        this.category = PanelCategory.EDITOR;
    }
}
