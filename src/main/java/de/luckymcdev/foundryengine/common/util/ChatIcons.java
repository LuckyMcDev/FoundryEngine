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

    static Entry[] values() {
        return new Entry[]{
                new Entry('.', "Small Space", SMALL_SPACE),
                new Entry('!', "Error", ERROR),
                new Entry('+', "Plus", PLUS),
                new Entry('-', "Minus", MINUS),
                new Entry('~', "Tilde", TILDE),
                new Entry('B', "Bubble", BUBBLE),
                new Entry('C', "Copy", COPY),
                new Entry('D', "ID", ID),
                new Entry('E', "Energy", ENERGY),
                new Entry('F', "Fire", FIRE),
                new Entry('H', "Heart", HEART),
                new Entry('h', "Half Heart", HALF_HEART),
                new Entry('I', "Info", INFO),
                new Entry('J', "Food", FOOD),
                new Entry('j', "Half Food", HALF_FOOD),
                new Entry('L', "Cold", COLD),
                new Entry('M', "Camera", CAMERA),
                new Entry('O', "Poison", POISON),
                new Entry('P', "Prototype Component", PROTOTYPE_COMPONENT),
                new Entry('Q', "Patched Component", PATCHED_COMPONENT),
                new Entry('T', "Tag", TAG),
                new Entry('W', "Warn", WARN),
                new Entry('X', "No", NO),
                new Entry('Y', "Yes", YES),
        };
    }

    record Entry(char character, String name, Component component) {}
}
