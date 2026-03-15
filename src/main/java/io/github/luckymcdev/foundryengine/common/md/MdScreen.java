package io.github.luckymcdev.foundryengine.common.md;

import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

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
        int titleHeight = 20;

        ScrollableLayout scrollableLayout = getLayout(padding, titleHeight);
        scrollableLayout.setMinWidth(this.width - (padding * 2));
        scrollableLayout.setMaxHeight(this.height - (padding * 2) - titleHeight);
        scrollableLayout.setX(padding);
        scrollableLayout.setY(padding + titleHeight);
        scrollableLayout.arrangeElements();

        scrollableLayout.visitWidgets(this::addRenderableWidget);
    }

    private @NonNull ScrollableLayout getLayout(int padding, int titleHeight) {
        LinearLayout contentLayout = LinearLayout.vertical();

        MultiLineTextWidget textWidget = new MultiLineTextWidget(
                markdownComponent,
                this.font
        );

        textWidget.setMaxWidth(this.width - (padding * 2) - 20);

        contentLayout.addChild(textWidget);

        contentLayout.arrangeElements();

        return new ScrollableLayout(
                this.minecraft,
                contentLayout,
                this.height - (padding * 2) - titleHeight
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}