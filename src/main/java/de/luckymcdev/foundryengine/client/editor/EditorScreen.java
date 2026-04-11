package de.luckymcdev.foundryengine.client.editor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EditorScreen extends Screen {
    private final boolean close;

    public EditorScreen(boolean close) {
        super(Component.translatable("gui.foundryengine.screen.editor"));
        this.close = close;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return close;
    }
}