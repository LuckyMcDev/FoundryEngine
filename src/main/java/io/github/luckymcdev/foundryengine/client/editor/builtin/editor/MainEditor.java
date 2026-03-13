package io.github.luckymcdev.foundryengine.client.editor.builtin.editor;

import imgui.flag.ImGuiKey;
import io.github.luckymcdev.foundryengine.client.util.key.Shortcut;
import io.github.luckymcdev.foundryengine.common.Common;

/**
 * The Main Editor which has all the functionality for the generation
 * see {@link EditorPanel}
 */
public class MainEditor extends EditorPanel {
    public static final MainEditor INSTANCE = new MainEditor();


    private MainEditor() {
        super(Common.id("main_editor"), "Main Editor", Shortcut.ctrl(ImGuiKey.E));
    }
}
