package de.luckymcdev.foundryengine.client.editor.builtin;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.Panel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import de.luckymcdev.foundryengine.client.ui.ExampleScreen;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.TestPacket;
import imgui.ImGui;
import imgui.extension.imguiknobs.ImGuiKnobs;
import imgui.flag.ImGuiKey;
import imgui.type.ImFloat;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * A simple Test Panel.
 * Shows some weird test stuff I guess.
 */
public class TestPanel extends Panel {
    public static final TestPanel INSTANCE = new TestPanel();
    private final ImFloat knobValue = new ImFloat(0.5f);

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

        ImGuiKnobs.knob("This Will Damage you if you press that button!", knobValue, 0.0f, 10.0f);


        if (ImGui.button("Press this!")) {
            ClientPacketDistributor.sendToServer(new TestPacket(knobValue.get()));
        }

        if (ImGui.button("Open Screen")) {
            Client.setScreen(new ExampleScreen());
        }

    }
}
