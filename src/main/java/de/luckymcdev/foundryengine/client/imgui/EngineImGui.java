package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.platform.Window;
import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;
public interface EngineImGui {
    void create(final Window window);

    void enable();

    void disable();

    void toggle();

    boolean isEnabled();

    void setTheme(ImTheme theme);

    void setTheme(ImTheme theme, boolean saveToConfig);

    ImTheme getCurrentTheme();

    void begin();

    void end();

    ImGuiFontManager getFontManager();

    boolean shouldInterceptMouse();

    boolean shouldInterceptKeyboard();

    void free();

    ImGuiGraphicsStack getGraphicsStack();

    int getDockId();

    void showMenuBar();

    void hideMenuBar();

    void toggleMenuBar();

    boolean isMenuBarVisible();
}
