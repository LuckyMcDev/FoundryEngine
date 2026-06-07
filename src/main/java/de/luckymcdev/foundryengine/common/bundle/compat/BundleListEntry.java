package de.luckymcdev.foundryengine.common.bundle.compat;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ModListScreen;

public class BundleListEntry extends ObjectSelectionList.Entry<BundleListEntry> {
    private static final Color COLOR_NAME = new Color(255, 255, 255);
    private static final Color COLOR_VERSION = new Color(204, 204, 204);
    private static final Color COLOR_TAG_BG = new Color(42, 74, 122);
    private static final Color COLOR_TAG_FG = new Color(138, 180, 232);

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
                x + 3, y + 2, COLOR_NAME.argb(), false);
        gfx.text(font, Component.literal(version).getVisualOrderText(),
                x + 3, y + 2 + font.lineHeight, COLOR_VERSION.argb(), false);

        String tagText = "B";
        int tagW = font.width(tagText) + 10;
        int tagH = font.lineHeight + 4;
        int tagX = getX() + getWidth() - tagW - 4;
        int tagY = y + 1;

        gfx.fill(tagX, tagY, tagX + tagW, tagY + tagH, COLOR_TAG_BG.argb());
        gfx.text(font, Component.literal(tagText).getVisualOrderText(),
                tagX + 5, tagY + 2, COLOR_TAG_FG.argb(), false);
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