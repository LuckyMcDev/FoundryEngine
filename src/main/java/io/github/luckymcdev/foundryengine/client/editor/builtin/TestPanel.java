package io.github.luckymcdev.foundryengine.client.editor.builtin;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import io.github.luckymcdev.foundryengine.client.util.Shortcut;
import io.github.luckymcdev.foundryengine.common.Common;

/**
 * A simple Test Panel.
 * Shows some weird test stuff i guess.
 */
public class TestPanel extends Panel {
    public static final TestPanel INSTANCE = new TestPanel();

    private TestPanel() {
        super(Common.id("test_panel"), "My Panel", Shortcut.empty());
    }

    @Override
    public void content() {
        ImGui.text("Hello, World!");

        ImGui.separator();

        ImGui.text("You dont know what i do? Hover it.");
        EngineImGuiUtils.helpTooltip("BOO!");

        EngineImGuiUtils.h1(() -> ImGui.text("BIG"));

        EngineImGuiUtils.textCentered("center center", ImGui.getWindowWidth());

        EngineImGuiUtils.resourceLocation(Common.id("imguiiscool"));

    }
}
