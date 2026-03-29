package de.luckymcdev.foundryengine.client.editor.styles.theme.builtin;

import de.luckymcdev.foundryengine.client.editor.styles.theme.ImTheme;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import net.minecraft.util.ARGB;

public class VidlibTheme implements ImTheme {
    public static void setColor(ImGuiStyle style, int key, int color) {
        style.setColor(key, ARGB.toABGR(color));
    }

    @Override
    public String getName() {
        return "vidlib";
    }

    @Override
    public void applyTheme(ImGuiStyle style) {
        style.setWindowPadding(4F, 4F);
        style.setFramePadding(4F, 1F);
        style.setPopupBorderSize(0F);
        style.setItemSpacing(6F, 4F);
        style.setItemInnerSpacing(8F, 6F);

        style.setWindowMenuButtonPosition(ImGuiDir.None);
        style.setWindowRounding(4F);
        style.setFrameRounding(3F);
        style.setChildRounding(3F);
        style.setPopupRounding(3F);
        style.setScrollbarRounding(1F);
        style.setGrabRounding(2F);
        style.setIndentSpacing(25F);
        style.setScrollbarSize(13F);
        style.setGrabMinSize(16F);
        style.setWindowBorderSize(0F);
        style.setSelectableTextAlign(0F, 0.5F);
        style.setAlpha(1F);

        setColor(style, ImGuiCol.WindowBg, 0xFF222228);
        setColor(style, ImGuiCol.PopupBg, 0xE30D0D11);
        setColor(style, ImGuiCol.FrameBg, 0xFF15151C);
        setColor(style, ImGuiCol.TitleBg, 0xFF010101);
        setColor(style, ImGuiCol.TitleBgActive, 0xFF010101);
        setColor(style, ImGuiCol.MenuBarBg, 0xFF222228);
        setColor(style, ImGuiCol.TitleBgCollapsed, 0xEF517F70);
        setColor(style, ImGuiCol.SliderGrab, 0xFF446692);
        setColor(style, ImGuiCol.Button, 0x664296FA);
        setColor(style, ImGuiCol.ButtonHovered, 0x664296FA);
        setColor(style, ImGuiCol.ButtonActive, 0x664296FA);
    }

}
