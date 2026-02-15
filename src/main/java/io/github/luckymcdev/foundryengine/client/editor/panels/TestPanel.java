package io.github.luckymcdev.foundryengine.client.editor.panels;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.common.Commons;

/**
 * The Test Panel Panel
 */
public class TestPanel extends Panel {
    /**
     * The constant INSTANCE.
     */
    public static final TestPanel INSTANCE = new TestPanel();

    private TestPanel() {
        super(Commons.id("test_panel"), "My Panel");
    }

    @Override
    public void content() {
        // Render the panel's content
        ImGui.text("Hello, World!");
    }
}
