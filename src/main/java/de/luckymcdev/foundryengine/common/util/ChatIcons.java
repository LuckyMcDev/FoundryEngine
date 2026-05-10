package de.luckymcdev.foundryengine.common.util;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;

/**
 * Holy goated mod Lat
 */
public interface ChatIcons {
    Style ICONS = Style.EMPTY.withFont(new FontDescription.Resource(Common.id("icons"))).applyFormat(ChatFormatting.WHITE);
    Component SMALL_SPACE = icon('.');
    Component ERROR = icon('!');
    Component PLUS = icon('+');
    Component MINUS = icon('-');
    Component TILDE = icon('~');
    Component BUBBLE = icon('B');
    Component COPY = icon('C');
    Component ID = icon('D');
    Component ENERGY = icon('E');
    Component FIRE = icon('F');
    Component HEART = icon('H');
    Component HALF_HEART = icon('h');
    Component INFO = icon('I');
    Component FOOD = icon('J');
    Component HALF_FOOD = icon('j');
    Component COLD = icon('L');
    Component CAMERA = icon('M');
    Component POISON = icon('O');
    Component PROTOTYPE_COMPONENT = icon('P');
    Component PATCHED_COMPONENT = icon('Q');
    Component TAG = icon('T');
    Component WARN = icon('W');
    Component NO = icon('X');
    Component YES = icon('Y');

    private static Component icon(char c) {
        return Component.literal(String.valueOf(c)).setStyle(ICONS);
    }
}
