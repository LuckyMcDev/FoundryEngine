package de.luckymcdev.foundryengine.client.editor.panel.test;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.editor.panel.tools.CataloguePanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.ui.ExampleScreen;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.font.BuiltInFonts;
import de.luckymcdev.foundryengine.common.network.packets.TestPacket;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.extension.imguiknobs.ImGuiKnobs;
import imgui.flag.ImGuiColorEditFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.slf4j.Logger;

public class TestPanel extends Panel {
    public static final TestPanel INSTANCE = new TestPanel();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ImFloat FLOAT = new ImFloat();
    private static final ImFloat FLOAT2 = new ImFloat();
    private static final ImBoolean BOOLEAN = new ImBoolean();
    private CataloguePanel.CataloguePayload cataloguePayload;

    private TestPanel() {
        super(new Builder(Common.id("test_panel"))
                .icon(ImIcons.FAE.FAE_BACTERIA)
                .category(PanelCategory.OPEN));
    }

    @Override
    public void content(ImGraphicsExtractor g) {
        if (!requireLocal()) return;

        ImGui.text("Hello, World!");
        ImGui.separator();

        ImGui.text("You dont know what i do? Hover it.");
        g.helpTooltip("BOO!");

        g.withFont(BuiltInFonts.LIGHT, () -> ImGui.text("Hello World (Light)"));
        g.withFont(BuiltInFonts.REGULAR, () -> ImGui.text("Hello World (Regular)"));
        g.withFont(BuiltInFonts.MEDIUM, () -> ImGui.text("Hello World (Medium)"));
        g.withFont(BuiltInFonts.SEMIBOLD, () -> ImGui.text("Hello World (SemiBold)"));
        g.withFont(BuiltInFonts.BOLD, () -> ImGui.text("Hello World (Bold)"));
        g.withFont(BuiltInFonts.ITALIC, () -> ImGui.text("Hello World (Italic)"));
        g.withFont(BuiltInFonts.BOLD_ITALIC, () -> ImGui.text("Hello World (Bold Italic)"));

        g.textCentered("center center", ImGui.getWindowWidth());
        g.identifier(Common.id("imguiiscool"));

        ImGuiKnobs.knob("This Will Damage you if you press that button!", FLOAT, 0.0f, 10.0f);
        ImGui.progressBar(FLOAT.get() / 10, ImGui.getContentRegionAvailX(), 0, "Progress");

        if (ImGui.button("Press this!")) {
            Common.getNetworkManager().sendToServer(new TestPacket(FLOAT.get()));
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
            this.cataloguePayload = data;
            LOGGER.info("Dropped catalogue item: {}", data.displayName());
        });

        if (cataloguePayload != null) {
            ImGui.text("Dropped: " + cataloguePayload.type() + " - " + cataloguePayload.id());
            float wh = 64;
            g.drawImage(cataloguePayload.texture().glId(), wh, wh);
        } else {
            ImGui.text("Drop something from the Catalogue panel!");
        }

        ImGui.colorPicker3("Pick a color", FLOAT2.getData(), ImGuiColorEditFlags.PickerHueWheel);

        ImGui.separator();

        g.component(Component.literal("Hello, World!").withColor(Color.RED.argb()));
        g.component(Component.literal("This is a Component being rendered. BOLD!").withStyle(Style.EMPTY.withBold(true)));
        g.component(Component.literal("This is a Component being rendered. ITALIC!").withStyle(Style.EMPTY.withItalic(true)));
        g.component(Component.literal("This is a Component being rendered. BOLD ITALIC!").withStyle(Style.EMPTY.withBold(true).withItalic(true)));
        g.component(Component.translatable("key.category.jei.hover.config.button"));
    }
}
