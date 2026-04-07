package de.luckymcdev.foundryengine.client.editor.builtin;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.Panel;
import de.luckymcdev.foundryengine.client.editor.builtin.tools.CataloguePanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
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
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.slf4j.Logger;

/**
 * A simple Test Panel.
 * Shows some weird test stuff I guess.
 */
public class TestPanel extends Panel {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final TestPanel INSTANCE = new TestPanel();
    private static final ImFloat FLOAT = new ImFloat();
    private static final ImFloat FLOAT2 = new ImFloat();
    private static final ImBoolean BOOLEAN = new ImBoolean();

    private TestPanel() {
        super(Common.id("test_panel"), "My Panel", ImIcons.DEV.DEV_PYTEST, Shortcut.ctrl(ImGuiKey.F5));
        this.category = PanelCategory.OPEN;
    }

    @Override
    public void content() {
        if (ImGuiUtils.requiresActiveSession()) return;

        ImGui.text("Hello, World!");

        ImGui.separator();

        ImGui.text("You dont know what i do? Hover it.");
        ImGuiUtils.helpTooltip("BOO!");

        ImGuiUtils.h1(() -> ImGui.text("BIG"));

        ImGuiUtils.textCentered("center center", ImGui.getWindowWidth());

        ImGuiUtils.resourceLocation(Common.id("imguiiscool"));

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

        ImGui.button("Drop Zone", -1, 50);
        CataloguePanel.acceptDrop(data -> {
            if (data.type().equals("blocks")) {
                LOGGER.info("It's a block: {}", data.id());
            } else {
                LOGGER.info("It's an item: {}", data.id());
            }

            if (data.tags().contains("foundry")) {
                LOGGER.info("This has the foundry tag!");
            }
        });

        ImGui.separator();
        ImGui.text("Post Processing Effects");

        renderEffectToggle("Blur", Identifier.withDefaultNamespace("blur"), 100);
        renderEffectToggle("Invert", Identifier.withDefaultNamespace("invert"), 50);
        renderEffectToggle("Creeper", Identifier.withDefaultNamespace("creeper"), 10);
        renderEffectToggle("Spider", Identifier.withDefaultNamespace("spider"), 10);
        renderEffectToggle("Greyscale Test", Common.id("grayscale"), 100);
        ImGui.text("these next ones require some special targets so idk when they work :D");
        renderEffectToggle("Entity Outline", Identifier.withDefaultNamespace("entity_outline"), 80);
        renderEffectToggle("Transparency", Identifier.withDefaultNamespace("transparency"), -1);

        if (ImGui.button("Reset All Effects")) {
            Client.getEffectManager().clearAllEffects();
        }
        ImGui.separator();

        ImGui.colorPicker3("Pick a color", FLOAT2.getData(), ImGuiColorEditFlags.PickerHueWheel);

    }

    /**
     * Helper to render a checkbox that communicates with the EffectManager
     */
    private void renderEffectToggle(String label, Identifier id, int priority) {
        boolean active = Client.getEffectManager().getActiveEffects().contains(id);
        ImBoolean check = new ImBoolean(active);
        if (ImGui.checkbox(label + " (Pri: " + priority + ")", check)) {
            Client.getEffectManager().setEffectActive(id, priority, check.get());
        }
    }
}
