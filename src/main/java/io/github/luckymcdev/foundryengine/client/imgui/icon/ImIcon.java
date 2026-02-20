package io.github.luckymcdev.foundryengine.client.imgui.icon;

/**
 * A Wrapper around a custom Char which is an icon.
 */
public interface ImIcon {
    char getIconChar();

    default char toChar() {
        return getIconChar();
    }

    default String iconName() {
        return ((Enum<?>) this).name();
    }
}