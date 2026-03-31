package de.luckymcdev.foundryengine.client.editor.builtin;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.Panel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import de.luckymcdev.foundryengine.client.ui.ExampleScreen;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.TestPacket;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.extension.imguiknobs.ImGuiKnobs;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiKey;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * A simple Test Panel.
 * Shows some weird test stuff I guess.
 */
public class TestPanel extends Panel {
    public static final TestPanel INSTANCE = new TestPanel();
    private static final ImFloat FLOAT = new ImFloat();
    private static final ImFloat FLOAT2 = new ImFloat();
    private static final ImBoolean BOOLEAN = new ImBoolean();

    private TestPanel() {
        super(Common.id("test_panel"), "My Panel", Shortcut.ctrl(ImGuiKey.F5));
        this.category = PanelCategory.OPEN;
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

        ImGuiKnobs.knob("This Will Damage you if you press that button!", FLOAT, 0.0f, 10.0f);

        ImGui.progressBar(FLOAT.get() / 10, ImGui.getContentRegionAvailX(), 0, "Progress");

        if (ImGui.button("Press this!")) {
            ClientPacketDistributor.sendToServer(new TestPacket(FLOAT.get()));
        }

        if (ImGui.button("Open Screen")) {
            Client.setScreen(new ExampleScreen());
        }

        if (ImGui.radioButton("Radio goo goo radio ga ga", BOOLEAN.get())) {
            BOOLEAN.set(!BOOLEAN.get());
        }

        if (ImGui.colorButton("Color Button?", Color.ORANGE.r(), Color.ORANGE.g(), Color.ORANGE.b(), Color.ORANGE.a())) {
            ImGui.text("COLOR BUTTON");
        }

        ImGui.colorPicker3("Pick a color", FLOAT2.getData(), ImGuiColorEditFlags.PickerHueWheel);

    }
}
