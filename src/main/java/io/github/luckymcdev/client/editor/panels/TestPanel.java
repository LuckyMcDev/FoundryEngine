package io.github.luckymcdev.client.editor.panels;

import imgui.ImGui;
import io.github.luckymcdev.client.editor.Panel;
import io.github.luckymcdev.common.Commons;

public class TestPanel extends Panel {
    public static final TestPanel INSTANCE = new TestPanel();

    public TestPanel() {
        super(Commons.id("test_panel"), "My Panel");
    }

    @Override
    public void content() {
        // Render the panel's content
        ImGui.text("Hello, World!");
    }
}
