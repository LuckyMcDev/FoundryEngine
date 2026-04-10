package de.luckymcdev.foundryengine.common.bundle.compat;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ModListScreen;

public class BundleListEntry extends ObjectSelectionList.Entry<BundleListEntry> {
    private static final int COLOR_NAME = 0xFFFFFFFF;
    private static final int COLOR_VERSION = 0xFFCCCCCC;
    private static final int COLOR_TAG_BG = 0xFF2A4A7A;
    private static final int COLOR_TAG_FG = 0xFF8AB4E8;

    private final Bundle bundle;
    private final ModListScreen parent;
    private final Font font;

    public BundleListEntry(Bundle bundle, ModListScreen parent) {
        this.bundle = bundle;
        this.parent = parent;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void extractContent(GuiGraphicsExtractor gfx, int mouseX, int mouseY, boolean hovered, float partialTick) {
        int x = getContentX();
        int y = getContentY();

        String name = bundle.info().displayName();
        String version = bundle.info().versionInfo().toString();

        gfx.text(font, Component.literal(name).getVisualOrderText(),
                x + 3, y + 2, COLOR_NAME, false);
        gfx.text(font, Component.literal(version).getVisualOrderText(),
                x + 3, y + 2 + font.lineHeight, COLOR_VERSION, false);

        String tagText = "B";
        int tagW = font.width(tagText) + 10;
        int tagH = font.lineHeight + 4;
        int tagX = getX() + getWidth() - tagW - 4;
        int tagY = y + 1;

        gfx.fill(tagX, tagY, tagX + tagW, tagY + tagH, COLOR_TAG_BG);
        gfx.text(font, Component.literal(tagText).getVisualOrderText(),
                tagX + 5, tagY + 2, COLOR_TAG_FG, false);
    }

    @Override
    public Component getNarration() {
        return Component.literal(bundle.info().displayName() + ", bundle");
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        ((BundleSelectable) parent).engine$setSelectedBundle(bundle);
        return true;
    }
}