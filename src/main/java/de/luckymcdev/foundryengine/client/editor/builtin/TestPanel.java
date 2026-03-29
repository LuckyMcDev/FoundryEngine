package de.luckymcdev.foundryengine.client.editor.builtin;

import de.luckymcdev.foundryengine.client.editor.Panel;
import de.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.extension.imguiknobs.ImGuiKnobs;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.type.ImFloat;

/**
 * A simple Test Panel.
 * Shows some weird test stuff I guess.
 */
public class TestPanel extends Panel {
    public static final TestPanel INSTANCE = new TestPanel();
    private final ImFloat knobValue = new ImFloat(0.5f);

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

        ImGuiKnobs.knob("knob", knobValue, 0.0f, 1.0f);

        TextEditor editor = new TextEditor();

        editor.setLanguageDefinition(TextEditorLanguageDefinition.C());
        editor.setText("TEST");

        editor.render("Test Editor");

    }
}
