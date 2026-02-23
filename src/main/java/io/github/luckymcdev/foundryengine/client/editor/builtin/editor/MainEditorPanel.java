package io.github.luckymcdev.foundryengine.client.editor.builtin.editor;

import imgui.flag.ImGuiKey;
import io.github.luckymcdev.foundryengine.client.util.Shortcut;
import io.github.luckymcdev.foundryengine.common.Common;

public class MainEditorPanel extends EditorPanel {
    public static final MainEditorPanel INSTANCE = new MainEditorPanel();


    private MainEditorPanel() {
        super(Common.id("main_editor"), "Main Editor", Shortcut.ctrl(ImGuiKey.E));
    }
}
