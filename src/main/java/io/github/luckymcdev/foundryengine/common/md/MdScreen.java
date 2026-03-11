package io.github.luckymcdev.foundryengine.common.md;

import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MdScreen extends Screen {
    private final Component markdownComponent;

    public MdScreen(Component title, Component md) {
        super(title);
        this.markdownComponent = md;
    }

    @Override
    protected void init() {
        super.init();

        int padding = 20;
        MultiLineTextWidget textWidget = new MultiLineTextWidget(
                padding,
                padding + 20,
                markdownComponent,
                this.font
        );

        textWidget.setMaxWidth(this.width - (padding * 2));

        this.addRenderableWidget(textWidget);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}