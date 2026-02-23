package io.github.luckymcdev.foundryengine.client.editor.builtin;

import imgui.ImGui;
import imgui.flag.ImGuiKey;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import io.github.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import io.github.luckymcdev.foundryengine.client.util.Shortcut;
import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.common.color.Color;

/**
 * A simple Test Panel.
 */
public class TestPanel extends Panel {
    public static final TestPanel INSTANCE = new TestPanel();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private TestPanel() {
        super(Common.id("test_panel"), "My Panel", Shortcut.ctrl(ImGuiKey.T));
    }

    /**
     * Renders Main Content.
     */
    @Override
    public void content() {
        // Render the panel's content
        ImGui.text("Hello, World!");


        ImGui.separator();

        ImGui.text("You dont know what i do? Hover it.");
        EngineImGuiUtils.helpTooltip("BOO!");

        EngineImGuiUtils.icon(ImIcons.FA.FA_NAVICON);

        EngineImGuiUtils.icon(ImIcons.FA.FA_NAVICON, Color.RED);

        EngineImGuiUtils.h1(() -> ImGui.text("BIG"));

        EngineImGuiUtils.textCentered("center center", ImGui.getWindowWidth());

        EngineImGuiUtils.resourceLocation(Common.id("imguiiscool"));

    }
}
