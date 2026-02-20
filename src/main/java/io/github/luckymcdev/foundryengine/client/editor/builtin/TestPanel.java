package io.github.luckymcdev.foundryengine.client.editor.builtin;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.common.Commons;

/**
 * A simple Test Panel.
 */
public class TestPanel extends Panel {
    public static final TestPanel INSTANCE = new TestPanel();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private TestPanel() {
        super(Commons.id("test_panel"), "My Panel");
    }

    /**
     * Renders Main Content.
     */
    @Override
    public void content() {
        // Render the panel's content
        ImGui.text("Hello, World!");
    }
}
